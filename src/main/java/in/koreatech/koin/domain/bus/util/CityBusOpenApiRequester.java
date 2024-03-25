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
import java.time.LocalTime;
import java.util.ArrayList;
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
import in.koreatech.koin.domain.bus.model.BusInfoCache;
import in.koreatech.koin.domain.bus.model.BusStationNode;
import in.koreatech.koin.domain.bus.model.CityBus;
import in.koreatech.koin.domain.bus.model.CityBusArrivalInfo;
import in.koreatech.koin.domain.bus.model.CityBusCache;
import in.koreatech.koin.domain.bus.repository.CityBusCacheRepository;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import lombok.RequiredArgsConstructor;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스도착정보
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098530
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityBusOpenApiRequester extends BusOpenApiRequester<CityBus> {

    private static final String ENCODE_TYPE = "UTF-8";
    private static final String CHEONAN_CITY_CODE = "34010";

    @Value("${OPEN_API_KEY}")
    private String OPEN_API_KEY;

    private final Gson gson;

    private final VersionRepository versionRepository;
    private final CityBusCacheRepository cityBusCacheRepository;

    private final Clock clock;

    private static final Type arrivalInfoType = new TypeToken<List<CityBusArrivalInfo>>() {
    }.getType();

    public List<CityBus> getBusRemainTime(String nodeId) {
        Version version = versionRepository.getByType(VersionType.CITY);

        Duration duration = Duration.between(version.getUpdatedAt().toLocalTime(), LocalTime.now(clock));

        if (0 <= duration.toSeconds() && duration.toSeconds() < 60) {
            return getCityBusArrivalInfoByCache(nodeId);
        }
        return getCityBusArrivalInfoByOpenApi(nodeId);
    }

    private List<CityBus> getCityBusArrivalInfoByCache(String nodeId) {
        CityBusCache cityBusCache = cityBusCacheRepository.findById(nodeId).orElse(null);
        if (cityBusCache != null) {
            return cityBusCache.getBusInfos().stream().map(CityBus::from).toList();
        }

        return new ArrayList<>();
    }

    private List<CityBus> getCityBusArrivalInfoByOpenApi(String nodeId) {
        getAllCityBusArrivalInfoByOpenApi();
        return getCityBusArrivalInfoByCache(nodeId);
    }

    private void getAllCityBusArrivalInfoByOpenApi() {
        List<List<CityBusArrivalInfo>> arrivalInfosList = BusStationNode.getNodeIds().stream()
            .map(this::getOpenApiResponse)
            .map(this::extractBusArrivalInfo)
            .toList();

        Version version = versionRepository.getByType(VersionType.CITY);
        version.update(Clock.systemDefaultZone());

        for (List<CityBusArrivalInfo> arrivalInfos : arrivalInfosList) {
            if (arrivalInfos.isEmpty())
                continue;

            cityBusCacheRepository.save(
                CityBusCache.create(
                    arrivalInfos.get(0).nodeid(),
                    arrivalInfos.stream()
                        .map(busArrivalInfo -> BusInfoCache.from(busArrivalInfo, version.getUpdatedAt()))
                        .toList()
                )
            );
        }

        // versionRepository.getByType(VersionType.CITY).update(Clock.systemDefaultZone());
    }

    private String getOpenApiResponse(String nodeId) {
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
        } catch (IOException e) {
            return null;
        }
    }

    private String getRequestURL(String cityCode, String nodeId) throws UnsupportedEncodingException {
        String url = "http://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList";
        String contentCount = "30";
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?" + encode("serviceKey", ENCODE_TYPE) + "=" + encode(OPEN_API_KEY, ENCODE_TYPE));
        urlBuilder.append("&" + encode("numOfRows", ENCODE_TYPE) + "=" + encode(contentCount, ENCODE_TYPE));
        urlBuilder.append("&" + encode("cityCode", ENCODE_TYPE) + "=" + encode(cityCode, ENCODE_TYPE));
        urlBuilder.append("&" + encode("nodeId", ENCODE_TYPE) + "=" + encode(nodeId, ENCODE_TYPE));
        urlBuilder.append("&_type=json");
        return urlBuilder.toString();
    }

    private List<CityBusArrivalInfo> extractBusArrivalInfo(String jsonResponse) {
        List<CityBusArrivalInfo> result = new ArrayList<>();

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
                result.add(gson.fromJson(item, CityBusArrivalInfo.class));
            }
            return result;
        } catch (JsonSyntaxException e) {
            return result;
        }
    }

    private void validateResponse(JsonObject response) {
        String resultCode = response.get("header").getAsJsonObject().get("resultCode").getAsString();

        String errorMessage = "";
        if ("12".equals(resultCode)) {
            errorMessage = "버스도착정보 공공 API 서비스가 폐기되었습니다.";
        }
        if ("20".equals(resultCode)) {
            errorMessage = "버스도착정보 공공 API 서비스가 접근 거부 상태입니다.";
        }
        if ("22".equals(resultCode)) {
            errorMessage = "버스도착정보 공공 API 서비스의 요청 제한 횟수가 초과되었습니다.";
        }
        if ("30".equals(resultCode)) {
            errorMessage = "등록되지 않은 버스도착정보 공공 API 서비스 키입니다.";
        }
        if ("31".equals(resultCode)) {
            errorMessage = "버스도착정보 공공 API 서비스 키의 활용 기간이 만료되었습니다.";
        }
        if (!"00".equals(resultCode)) {
            String resultMessage = response.get("header").getAsJsonObject().get("resultMsg").getAsString();
            throw BusOpenApiException.withDetail(errorMessage + " resultMsg: " + resultMessage);
        }
    }
}
