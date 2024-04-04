package in.koreatech.koin.domain.bus.util;

import static java.net.URLEncoder.encode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.IntercityBusArrival;
import in.koreatech.koin.domain.bus.model.IntercityBusRoute;
import in.koreatech.koin.domain.bus.model.enums.BusOpenApiResultCode;
import in.koreatech.koin.domain.bus.model.enums.IntercityBusStationNode;
import in.koreatech.koin.domain.bus.model.redis.CityBusCache;
import in.koreatech.koin.domain.bus.model.redis.IntercityBusCache;
import in.koreatech.koin.domain.bus.model.redis.IntercityBusCacheInfo;
import in.koreatech.koin.domain.bus.repository.IntercityBusCacheRepository;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스도착정보
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098541
 */
@Component
@Transactional(readOnly = true)
public class IntercityBusOpenApiClient extends BusOpenApiClient<BusRemainTime> {

    private static final String ENCODE_TYPE = "UTF-8";
    private static final Type arrivalInfoType = new TypeToken<List<IntercityBusArrival>>() {
    }.getType();

    private final VersionRepository versionRepository;
    private final IntercityBusCacheRepository intercityBusCacheRepository;
    private final String openApiKey;
    private final Gson gson;
    private final Clock clock;

    public IntercityBusOpenApiClient(
        @Value("${OPEN_API_KEY}") String openApiKey,
        VersionRepository versionRepository,
        Gson gson,
        Clock clock,
        IntercityBusCacheRepository interCityBusCacheRepository
    ) {
        this.openApiKey = openApiKey;
        this.versionRepository = versionRepository;
        this.gson = gson;
        this.clock = clock;
        this.intercityBusCacheRepository = interCityBusCacheRepository;
    }

    public List<BusRemainTime> getBusRemainTime(String depTerminalId, String arrTerminalId) {
        Version version = versionRepository.getByType(VersionType.EXPRESS);

        if(isCacheExpired(version, clock)){
            getIntercityBusArrivalInfoByOpenApi();
        }

        return getInterCityBusArrivalInfoByCache(depTerminalId, arrTerminalId);
    }

    private List<BusRemainTime> getInterCityBusArrivalInfoByCache(String depTerminalId, String arrTerminalId){
        String busRoute = depTerminalId + ":" + arrTerminalId;
        Optional<IntercityBusCache> intercityBusCache = intercityBusCacheRepository.findById(busRoute);
        return intercityBusCache.map(busCache -> busCache.getBusInfos().stream().map(IntercityBusRemainTime::from).toLIst())
            .orElseGet(ArrayList::new);
    }

    private void getIntercityBusArrivalInfoByOpenApi() {
        List<List<IntercityBusArrival>> arrivalInfosList = IntercityBusStationNode.getIds().stream()
            .map(routeList -> getOpenApiResponse(routeList.depTerminalId(), routeList.depTerminalId()))
            .map(this::extractBusArrivalInfo)
            .toList();

        LocalDateTime updatedAt = LocalDateTime.now(clock);

        for (List<IntercityBusArrival> arrivalInfos : arrivalInfosList ) {
            if (arrivalInfos.isEmpty()){
                continue;
            }
            intercityBusCacheRepository.save(
                IntercityBusCache.create(
                    new IntercityBusRoute(arrivalInfos.get(0).depPlaceNm(), arrivalInfos.get(0).arrPlaceNm()),
                    arrivalInfos.stream()
                        .map(busArrivalInfo -> IntercityBusCacheInfo.from(busArrivalInfo, updatedAt))
                        .toList()
               )
            );
        }

        versionRepository.getByType(VersionType.EXPRESS).update(clock);
    }

    public String getOpenApiResponse(String depTerminalId, String arrTerminalId){
        try {
            URL url = new URL(getRequestURL(depTerminalId, arrTerminalId));
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-type", "application/json");

            BufferedReader input;
            if(con.getResponseCode() >= 200 && con.getResponseCode() <= 300) {
                input = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                input = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while((line = input.readLine()) != null) {
                response.append(line);
            }
            input.close();
            con.disconnect();
            return response.toString();
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    private String getRequestURL(String depTerminalId, String arrTerminalId) throws UnsupportedEncodingException {
        String url = "http://apis.data.go.kr/1613000/SuburbsBusInfoService/getStrtpntAlocFndSuberbsBusInfo";
        String contentCount = "30";
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?" + encode("serviceKey", ENCODE_TYPE) + "=" + encode(openApiKey, ENCODE_TYPE));
        urlBuilder.append("&" + encode("numOfRows", ENCODE_TYPE) + "=" + encode(contentCount, ENCODE_TYPE));
        urlBuilder.append("&" + encode("depTerminalId", ENCODE_TYPE) + "=" + encode(depTerminalId, ENCODE_TYPE));
        urlBuilder.append("&" + encode("arrTerminalId", ENCODE_TYPE) + "=" + encode(arrTerminalId, ENCODE_TYPE));
        urlBuilder.append(
            "&" + encode("depPlandTime", ENCODE_TYPE) + "=" + ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(
                DateTimeFormatter.ofPattern("yyyyMMdd")));
        urlBuilder.append("&_type=json");
        return urlBuilder.toString();
    }

    private List<IntercityBusArrival> extractBusArrivalInfo(String jsonResponse) {
        List<IntercityBusArrival> result = new ArrayList<>();

        try {
            JsonObject response = JsonParser.parseString(jsonResponse)
                .getAsJsonObject()
                .get("response")
                .getAsJsonObject();
            BusOpenApiResultCode.validateResponse(response);
            JsonObject body = response.get("body").getAsJsonObject();

            if (body.get("totalCount").getAsLong() == 0) {
                return result;
            }

            JsonElement item = body.get("items").getAsJsonObject().get("item");
            if (item.isJsonArray()) {
                return gson.fromJson(item, arrivalInfoType);
            }
            if (item.isJsonObject()) {
                result.add(gson.fromJson(item, IntercityBusArrival.class));
            }
            return result;
        } catch (JsonSyntaxException e) {
            return result;
        }
    }

    public boolean isCacheExpired(Version version, Clock clock) {   // 1시간 단위로 수정 필요
        Duration duration = Duration.between(version.getUpdatedAt().toLocalTime(), LocalTime.now(clock));
        return duration.toSeconds() < 0 || CityBusCache.getCacheExpireSeconds() <= duration.toSeconds();
    }
}
