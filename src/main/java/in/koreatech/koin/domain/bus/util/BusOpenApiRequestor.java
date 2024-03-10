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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import in.koreatech.koin.domain.bus.exception.BusOpenApiException;
import in.koreatech.koin.domain.bus.model.CityBusArrivalInfo;
import lombok.RequiredArgsConstructor;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스도착정보
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098530
 */
@Component
@RequiredArgsConstructor
public class BusOpenApiRequestor {

    private static final String ENCODE_TYPE = "UTF-8";
    private static final String CHEONAN_CITY_CODE = "34010";
    private static final List<Integer> AVAILABLE_CITY_BUS = List.of(400, 401, 402, 405, 815);

    @Value("${OPEN_API_KEY}")
    private String OPEN_API_KEY;

    private final Gson gson;

    private static final Type arrivalInfoType = new TypeToken<List<CityBusArrivalInfo>>() {
    }.getType();

    public List<CityBusArrivalInfo> getCityBusArrivalInfoByOpenApi(String nodeId) {
        List<CityBusArrivalInfo> arrivalInfos = extractBusArrivalInfo(getCityBusArrivalInfo(nodeId));
        arrivalInfos.removeIf(arrivalInfo -> !AVAILABLE_CITY_BUS.contains(arrivalInfo.getRouteno()));
        //TODO: 레디스 캐싱
        return arrivalInfos;
    }

    private String getCityBusArrivalInfo(String nodeId) {
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
        jsonResponse = "{\n"
            + "\"response\": {\n"
            + "\"header\": {\n"
            + "\"resultCode\": \"00\",\n"
            + "\"resultMsg\": \"NORMAL SERVICE.\"\n"
            + "},\n"
            + "\"body\": {\n"
            + "\"items\": {\n"
            + "\"item\": {\n"
            + "\"arrprevstationcnt\": 4,\n"
            + "\"arrtime\": 218,\n"
            + "\"nodeid\": \"CAB285000405\",\n"
            + "\"nodenm\": \"코리아텍\",\n"
            + "\"routeid\": \"CAB285000143\",\n"
            + "\"routeno\": 400,\n"
            + "\"routetp\": \"일반버스\",\n"
            + "\"vehicletp\": \"일반차량\"\n"
            + "}\n"
            + "},\n"
            + "\"numOfRows\": 30,\n"
            + "\"pageNo\": 1,\n"
            + "\"totalCount\": 1\n"
            + "}\n"
            + "}\n"
            + "}";
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
