package in.koreatech.koin.domain.bus.util;

import static java.net.URLEncoder.encode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.BusStationNode;
import in.koreatech.koin.domain.bus.model.CityBusArrivalInfo;
import in.koreatech.koin.domain.bus.model.CityBusCache;
import in.koreatech.koin.domain.bus.repository.CityBusCacheRepository;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import lombok.RequiredArgsConstructor;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스도착정보
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098530
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusOpenApiRequestor {

    private static final String ENCODE_TYPE = "UTF-8";
    private static final String CHEONAN_CITY_CODE = "34010";

    @Value("${OPEN_API_KEY}")
    private String OPEN_API_KEY;

    private final Gson gson;

    private final VersionRepository versionRepository;
    private final CityBusCacheRepository cityBusCacheRepository;

    private static final Type arrivalInfoType = new TypeToken<List<CityBusArrivalInfo>>() {
    }.getType();

    public List<BusRemainTime> getCityBusRemainTime(String nodeId) {
        if (cityBusCacheRepository.findById(nodeId).isPresent()) {
            return getCityBusArrivalInfoByCache(nodeId);
        }
        return getCityBusArrivalInfoByOpenApi(nodeId);
    }

    /**
     * 당장 해야하는 것: 버전 정보 저장
     *
     * 현재: 레디스에 각 노드별 정보를 비어있어도 전부 저장한다.
     * 구상: mysql에 버전 최신화 시각 정보를 기준으로 판단한다. -> 정보가 없는건 저장하지 않아도 된다. -> 성능 향상
     *
     *
     * BusRemainTIme 대신 ArrivalInfo를 하는게 확장성에 유리
     */

    private List<BusRemainTime> getCityBusArrivalInfoByCache(String nodeId) {
        CityBusCache cityBusCache = cityBusCacheRepository.getById(nodeId);
        return cityBusCache.getRemainTime().stream()
            .map(BusRemainTime::from)
            .collect(Collectors.toList());
    }

    private List<BusRemainTime> getCityBusArrivalInfoByOpenApi(String nodeId) {
        getAllCityBusArrivalInfoByOpenApi();
        CityBusCache cityBusCache = cityBusCacheRepository.getById(nodeId);
        return cityBusCache.getRemainTime().stream()
            .map(BusRemainTime::from)
            .toList();
    }

    private void getAllCityBusArrivalInfoByOpenApi() {
        List<List<CityBusArrivalInfo>> arrivalInfosList = BusStationNode.getNodeIds().stream()
            .map(this::getOpenApiResponse)
            .map(this::extractBusArrivalInfo)
            .toList();

        //TODO: 아래 내용 for문으로 수정(save는 forEach에서 하면 이슈 발생)
        arrivalInfosList.forEach(arrivalInfos ->
            cityBusCacheRepository.save(
                CityBusCache.create(
                    arrivalInfos.get(0).getNodeid(), //TODO: 비어있는 정보는 저장 전에 걸러내야 할 거임 (안그러면 에러남)
                    arrivalInfos.stream()
                        .map(CityBusArrivalInfo::getArrtime)
                        .toList()
                )
            )
        );
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
        String url = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getCrdntPrxmtSttnList";
        String contentCount = "30";
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?" + encode("serviceKey", ENCODE_TYPE) + "=" + encode(OPEN_API_KEY, ENCODE_TYPE));
        urlBuilder.append("&" + encode("numOfRows", ENCODE_TYPE) + "=" + encode(contentCount, ENCODE_TYPE));
        urlBuilder.append("&" + encode("cityCode", ENCODE_TYPE) + "=" + encode(cityCode, ENCODE_TYPE));
        urlBuilder.append("&" + encode("nodeId", ENCODE_TYPE) + "=" + encode(nodeId, ENCODE_TYPE));
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
