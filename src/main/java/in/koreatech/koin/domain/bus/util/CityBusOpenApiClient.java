package in.koreatech.koin.domain.bus.util;

import static in.koreatech.koin.domain.bus.model.Constant.*;
import static java.net.URLEncoder.encode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Clock;
import java.time.LocalDateTime;
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

import in.koreatech.koin.domain.bus.exception.BusOpenApiException;
import in.koreatech.koin.domain.bus.model.CityBusArrival;
import in.koreatech.koin.domain.bus.model.CityBusRemainTime;
import in.koreatech.koin.domain.bus.model.enums.BusStationNode;
import in.koreatech.koin.domain.bus.model.redis.BusCache;
import in.koreatech.koin.domain.bus.model.redis.CityBusCache;
import in.koreatech.koin.domain.bus.repository.CityBusCacheRepository;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스도착정보
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098530
 */
@Component
@Transactional(readOnly = true)
public class CityBusOpenApiClient extends BusOpenApiClient<CityBusRemainTime> {

    private static final String ENCODE_TYPE = "UTF-8";
    private static final String CHEONAN_CITY_CODE = "34010";

    private final String openApiKey;

    private final Gson gson;

    private final Clock clock;

    private final VersionRepository versionRepository;
    private final CityBusCacheRepository cityBusCacheRepository;

    private static final Type arrivalInfoType = new TypeToken<List<CityBusArrival>>() {
    }.getType();

    public CityBusOpenApiClient(
        @Value("${OPEN_API_KEY}") String openApiKey,
        Gson gson,
        Clock clock,
        VersionRepository versionRepository,
        CityBusCacheRepository cityBusCacheRepository
    ) {
        this.openApiKey = openApiKey;
        this.gson = gson;
        this.clock = clock;
        this.versionRepository = versionRepository;
        this.cityBusCacheRepository = cityBusCacheRepository;
    }

    public List<CityBusRemainTime> getBusRemainTime(String nodeId) {
        Version version = versionRepository.getByType(VersionType.CITY);

        if (isCacheExpired(version, clock)) {
            getAllCityBusArrivalInfoByOpenApi();
        }

        return getCityBusArrivalInfoByCache(nodeId);
    }

    private List<CityBusRemainTime> getCityBusArrivalInfoByCache(String nodeId) {
        Optional<CityBusCache> cityBusCache = cityBusCacheRepository.findById(nodeId);

        return cityBusCache.map(busCache -> busCache.getBusInfos().stream().map(CityBusRemainTime::from).toList())
            .orElseGet(ArrayList::new);
    }

    private void getAllCityBusArrivalInfoByOpenApi() {
        List<List<CityBusArrival>> arrivalInfosList = BusStationNode.getNodeIds().stream()
            .map(this::getOpenApiResponse)
            .map(this::extractBusArrivalInfo)
            .toList();

        LocalDateTime updatedAt = LocalDateTime.now(clock);

        for (List<CityBusArrival> arrivalInfos : arrivalInfosList) {
            if (arrivalInfos.isEmpty()) {
                continue;
            }

            cityBusCacheRepository.save(
                CityBusCache.create(
                    arrivalInfos.get(0).nodeid(),
                    arrivalInfos.stream()
                        .map(busArrivalInfo -> BusCache.from(busArrivalInfo, updatedAt))
                        .toList()
                )
            );
        }

        versionRepository.getByType(VersionType.CITY).update(clock);
    }

    public String getOpenApiResponse(String nodeId) {
        try {
            URL url = new URL(getRequestURL(CHEONAN_CITY_CODE, nodeId));
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");

            BufferedReader input;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                input = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = input.readLine()) != null) {
                response.append(line);
            }
            input.close();
            conn.disconnect();
            return response.toString();
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    private String getRequestURL(String cityCode, String nodeId) throws UnsupportedEncodingException {
        String url = "https://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList";
        String contentCount = "30";
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?" + encode("serviceKey", ENCODE_TYPE) + "=" + encode(openApiKey, ENCODE_TYPE));
        urlBuilder.append("&" + encode("numOfRows", ENCODE_TYPE) + "=" + encode(contentCount, ENCODE_TYPE));
        urlBuilder.append("&" + encode("cityCode", ENCODE_TYPE) + "=" + encode(cityCode, ENCODE_TYPE));
        urlBuilder.append("&" + encode("nodeId", ENCODE_TYPE) + "=" + encode(nodeId, ENCODE_TYPE));
        urlBuilder.append("&_type=json");
        return urlBuilder.toString();
    }

    private List<CityBusArrival> extractBusArrivalInfo(String jsonResponse) {
        List<CityBusArrival> result = new ArrayList<>();

        try {
            JsonObject response = JsonParser.parseString(jsonResponse)
                .getAsJsonObject()
                .get("response")
                .getAsJsonObject();
            validateResponse(response);
            JsonObject body = response.get("body").getAsJsonObject();

            if (body.get("totalCount").getAsLong() == 0) {
                return result;
            }

            JsonElement item = body.get("items").getAsJsonObject().get("item");
            if (item.isJsonArray()) {
                return gson.fromJson(item, arrivalInfoType);
            }
            if (item.isJsonObject()) {
                result.add(gson.fromJson(item, CityBusArrival.class));
            }
            return result;
        } catch (JsonSyntaxException e) {
            return result;
        }
    }

    private void validateResponse(JsonObject response) {
        String resultCode = response.get("header").getAsJsonObject().get("resultCode").getAsString();

        String errorMessage = "";
        if (CODE_SERVICE_DISPOSE.equals(resultCode)) {
            errorMessage = "버스도착정보 공공 API 서비스가 폐기되었습니다.";
        }
        if (CODE_SERVICE_ACCESS_DENIED.equals(resultCode)) {
            errorMessage = "버스도착정보 공공 API 서비스가 접근 거부 상태입니다.";
        }
        if (CODE_SERVICE_REQUEST_OVER.equals(resultCode)) {
            errorMessage = "버스도착정보 공공 API 서비스의 요청 제한 횟수가 초과되었습니다.";
        }
        if (CODE_KEY_UNREGISTERED.equals(resultCode)) {
            errorMessage = "등록되지 않은 버스도착정보 공공 API 서비스 키입니다.";
        }
        if (CODE_SERVICE_KEY_EXPIRED.equals(resultCode)) {
            errorMessage = "버스도착정보 공공 API 서비스 키의 활용 기간이 만료되었습니다.";
        }
        if (!CODE_SERVICE_SUCCESS.equals(resultCode)) {
            String resultMessage = response.get("header").getAsJsonObject().get("resultMsg").getAsString();
            throw BusOpenApiException.withDetail(errorMessage + " resultMsg: " + resultMessage);
        }
    }
}
