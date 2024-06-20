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
import in.koreatech.koin.domain.bus.model.enums.BusOpenApiResultCode;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCache;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCacheInfo;
import in.koreatech.koin.domain.bus.model.express.ExpressBusRoute;
import in.koreatech.koin.domain.bus.model.express.ExpressBusStationNode;
import in.koreatech.koin.domain.bus.model.express.PublicOpenApiExpressBusArrival;
import in.koreatech.koin.domain.bus.repository.ExpressBusCacheRepository;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import in.koreatech.koin.global.exception.KoinIllegalStateException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스도착정보
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098541
 */
@Component
public class PublicExpressBusClient extends ExpressBusClient {

    private static final String OPEN_API_URL = "https://apis.data.go.kr/1613000/SuburbsBusInfoService/getStrtpntAlocFndSuberbsBusInfo";
    private static final Type ARRIVAL_INFO_TYPE = new TypeToken<List<PublicOpenApiExpressBusArrival>>() {
    }.getType();

    private final String openApiKey;

    public PublicExpressBusClient(
        @Value("${OPEN_API_KEY_PUBLIC}") String openApiKey,
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
    @CircuitBreaker(name = "publicExpressBus")
    public void storeRemainTimeByOpenApi() {
        for (BusStation depart : BusStation.values()) {
            for (BusStation arrival : BusStation.values()) {
                if (depart == arrival) {
                    continue;
                }
                JsonObject openApiResponse;
                try {
                    openApiResponse = getOpenApiResponse(depart, arrival);
                } catch (BusOpenApiException e) {
                    continue;
                }
                List<PublicOpenApiExpressBusArrival> busArrivals = extractBusArrivalInfo(openApiResponse);

                ExpressBusCache expressBusCache = ExpressBusCache.of(
                    new ExpressBusRoute(depart.getName(), arrival.getName()),
                    // API로 받은 yyyyMMddHHmm 형태의 시간을 HH:mm 형태로 변환하여 Redis에 저장한다.
                    busArrivals.stream()
                        .map(it -> new ExpressBusCacheInfo(
                            LocalTime.parse(
                                LocalDateTime.parse(it.depPlandTime(), ofPattern("yyyyMMddHHmm"))
                                    .format(ofPattern("HH:mm"))
                            ),
                            LocalTime.parse(
                                LocalDateTime.parse(it.arrPlandTime(), ofPattern("yyyyMMddHHmm"))
                                    .format(ofPattern("HH:mm"))
                            ),
                            it.charge()
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
    protected JsonObject getOpenApiResponse(BusStation depart, BusStation arrival) {
        try {
            URL url = getBusApiURL(depart, arrival);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
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
        StringBuilder urlBuilder = new StringBuilder(OPEN_API_URL); /*URL*/
        try {
            urlBuilder.append("?" + encode("serviceKey", UTF_8) + "=" + encode(openApiKey, UTF_8));
            urlBuilder.append("&" + encode("numOfRows", UTF_8) + "=" + encode("30", UTF_8));
            urlBuilder.append("&" + encode("_type", UTF_8) + "=" + encode("json", UTF_8));
            urlBuilder.append("&" + encode("depTerminalId", UTF_8) + "=" + encode(departNode.getStationId(), UTF_8));
            urlBuilder.append("&" + encode("arrTerminalId", UTF_8) + "=" + encode(arrivalNode.getStationId(), UTF_8));
            urlBuilder.append("&" + encode("depPlandTime", UTF_8) + "="
                + encode(LocalDateTime.now(clock).format(ofPattern("yyyyMMdd")), UTF_8));
            return new URL(urlBuilder.toString());
        } catch (Exception e) {
            throw new KoinIllegalStateException("시외버스 API URL 생성중 문제가 발생했습니다.", "uri:" + urlBuilder);
        }
    }

    @Override
    protected List<PublicOpenApiExpressBusArrival> extractBusArrivalInfo(JsonObject jsonObject) {
        try {
            var response = jsonObject.get("response").getAsJsonObject();
            BusOpenApiResultCode.validateResponse(response);
            JsonObject body = response.get("body").getAsJsonObject();
            if (body.get("totalCount").getAsLong() == 0) {
                return Collections.emptyList();
            }
            JsonElement item = body.get("items").getAsJsonObject().get("item");
            List<PublicOpenApiExpressBusArrival> result = new ArrayList<>();
            if (item.isJsonArray()) {
                return gson.fromJson(item, ARRIVAL_INFO_TYPE);
            }
            if (item.isJsonObject()) {
                result.add(gson.fromJson(item, PublicOpenApiExpressBusArrival.class));
            }
            return result;
        } catch (JsonSyntaxException e) {
            return Collections.emptyList();
        }
    }
}