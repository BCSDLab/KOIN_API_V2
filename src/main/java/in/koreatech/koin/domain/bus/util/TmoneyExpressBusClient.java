package in.koreatech.koin.domain.bus.util;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ofPattern;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import in.koreatech.koin.domain.bus.exception.BusOpenApiException;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCache;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCacheInfo;
import in.koreatech.koin.domain.bus.model.express.ExpressBusRoute;
import in.koreatech.koin.domain.bus.model.express.ExpressBusStationNode;
import in.koreatech.koin.domain.bus.model.express.TmoneyOpenApiExpressBusArrival;
import in.koreatech.koin.domain.bus.repository.ExpressBusCacheRepository;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import in.koreatech.koin.global.exception.KoinIllegalStateException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * OpenApi 상세: 티머니 Api
 * https://apiportal.tmoney.co.kr:18443/apiGallery/apiGalleryDetail.do?apiId=API201906241410183kp&apiPckgId=APK2024051316462950w&isTestYn=Y
 */
@Component
public class TmoneyExpressBusClient extends ExpressBusClient {

    private static final String OPEN_API_URL = "https://apigw.tmoney.co.kr:5556/gateway/xzzIbtListGet/v1/ibt_list";
    private static final Type ARRIVAL_INFO_TYPE = new TypeToken<List<TmoneyOpenApiExpressBusArrival>>() {
    }.getType();

    private final String openApiKey;

    public TmoneyExpressBusClient(
        @Value("${OPEN_API_KEY_TMONEY}") String openApiKey,
        VersionRepository versionRepository,
        Gson gson,
        Clock clock,
        ExpressBusCacheRepository expressBusCacheRepository
    ) {
        super(versionRepository, gson, clock, expressBusCacheRepository);
        this.openApiKey = openApiKey;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "tmoneyExpressBus")
    public void storeRemainTimeByOpenApi() {
        for (BusStation depart : BusStation.values()) {
            for (BusStation arrival : BusStation.values()) {
                if (depart == arrival || depart.equals(BusStation.STATION) || arrival.equals(BusStation.STATION)) {
                    continue;
                }
                JsonObject openApiResponse;
                try {
                    openApiResponse = getOpenApiResponse(depart, arrival);
                } catch (BusOpenApiException e) {
                    continue;
                }
                List<TmoneyOpenApiExpressBusArrival> busArrivals = extractBusArrivalInfo(openApiResponse);

                ExpressBusCache expressBusCache = ExpressBusCache.of(
                    new ExpressBusRoute(depart.getName(), arrival.getName()),
                    // API로 받은 HHmm 형태의 시간을 HH:mm 형태로 변환하여 Redis에 저장한다.
                    busArrivals.stream()
                        .map(it -> new ExpressBusCacheInfo(
                            LocalTime.parse(it.TIM_TIM_O(), ofPattern("HHmm")),
                            LocalTime.parse(it.TIM_TIM_O(), ofPattern("HHmm")).plusMinutes(it.LIN_TIM()),
                            1900
                        ))
                        .toList()
                );
                if (!expressBusCache.getBusInfos().isEmpty()) {
                    expressBusCacheRepository.save(expressBusCache);
                }
            }
        }
        versionRepository.getByType(VersionType.EXPRESS).update(clock);
    }

    @Override
    protected List<TmoneyOpenApiExpressBusArrival> extractBusArrivalInfo(JsonObject jsonObject) {
        try {
            var code = jsonObject.get("code").getAsString();
            if (!code.equals("00000")) {
                return Collections.emptyList();
            }
            var response = jsonObject.get("response").getAsJsonObject();
            JsonElement departTimeList = response.get("LINE_LIST");
            List<TmoneyOpenApiExpressBusArrival> result = new ArrayList<>();
            if (departTimeList.isJsonArray()) {
                return gson.fromJson(departTimeList, ARRIVAL_INFO_TYPE);
            }
            if (departTimeList.isJsonObject()) {
                result.add(gson.fromJson(departTimeList, TmoneyOpenApiExpressBusArrival.class));
            }
            return result;
        } catch (JsonSyntaxException e) {
            return Collections.emptyList();
        }
    }

    @Override
    protected JsonObject getOpenApiResponse(BusStation depart, BusStation arrival) {
        try {
            URL url = getBusApiURL(depart, arrival);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-Gateway-APIKey", openApiKey);
            conn.setRequestProperty("Accept", "*/*");
            BufferedReader reader;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            conn.disconnect();
            return JsonParser.parseString(result.toString())
                .getAsJsonObject();
        } catch (Exception ignore) {
            throw BusOpenApiException.withDetail("depart: " + depart + " arrival: " + arrival);
        }
    }

    @Override
    protected URL getBusApiURL(BusStation depart, BusStation arrival) {
        ExpressBusStationNode departNode = ExpressBusStationNode.from(depart);
        ExpressBusStationNode arrivalNode = ExpressBusStationNode.from(arrival);
        LocalDateTime today = LocalDateTime.now(clock);
        StringBuilder urlBuilder = new StringBuilder(OPEN_API_URL); /*URL*/
        try {
            urlBuilder.append(String.format(
                "/%s/0000/%s/%s/9/0",
                encode(today.format(ofPattern("yyyyMMdd")), UTF_8),
                encode(departNode.getTmoneyStationId(), UTF_8),
                encode(arrivalNode.getTmoneyStationId(), UTF_8)
            ));
            return new URL(urlBuilder.toString());
        } catch (Exception e) {
            throw new KoinIllegalStateException("시외버스 API URL 생성중 문제가 발생했습니다.", "uri:" + urlBuilder);
        }
    }
}
