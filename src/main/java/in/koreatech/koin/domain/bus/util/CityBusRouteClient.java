package in.koreatech.koin.domain.bus.util;

import static java.net.URLEncoder.encode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import in.koreatech.koin.domain.bus.model.city.CityBusRoute;
import in.koreatech.koin.domain.bus.model.city.CityBusRouteCache;
import in.koreatech.koin.domain.bus.model.enums.BusOpenApiResultCode;
import in.koreatech.koin.domain.bus.model.enums.BusStationNode;
import in.koreatech.koin.domain.bus.repository.CityBusRouteCacheRepository;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스정류소정보 - 정류소별경유노선 목록조회
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098534
 */
@Component
@Transactional(readOnly = true)
public class CityBusRouteClient {

    private static final Set<Long> AVAILABLE_CITY_BUS = Set.of(400L, 402L, 405L);

    private static final String ENCODE_TYPE = "UTF-8";
    private static final String CHEONAN_CITY_CODE = "34010";
    private static final Type availableCityBusType = new TypeToken<List<CityBusRoute>>() {
    }.getType();

    private final String openApiKey;
    private final Gson gson;
    private final CityBusRouteCacheRepository cityBusRouteCacheRepository;

    public CityBusRouteClient(
        @Value("${OPEN_API_KEY_PUBLIC}") String openApiKey,
        Gson gson,
        CityBusRouteCacheRepository cityBusRouteCacheRepository
    ) {
        this.openApiKey = openApiKey;
        this.gson = gson;
        this.cityBusRouteCacheRepository = cityBusRouteCacheRepository;
    }

    public Set<Long> getAvailableCityBus(String nodeId) {
        Optional<CityBusRouteCache> routeCache = cityBusRouteCacheRepository.findById(nodeId);
        if (routeCache.isEmpty()) {
            return new HashSet<>(AVAILABLE_CITY_BUS);
        }

        return routeCache.get().getBusNumbers();
    }

    @Transactional
    public void storeCityBusRoute() {
        for (String node : BusStationNode.getNodeIds()) {
            cityBusRouteCacheRepository.save(
                CityBusRouteCache.of(
                    node,
                    Set.copyOf(extractBusRouteInfo(getOpenApiResponse(node)))
                )
            );
        }
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
        } catch (Exception e) {
            throw BusOpenApiException.withDetail("nodeId: " + nodeId);
        }
    }

    private String getRequestURL(String cityCode, String nodeId) throws UnsupportedEncodingException {
        String url = "https://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnThrghRouteList";
        String contentCount = "50";
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?" + encode("serviceKey", ENCODE_TYPE) + "=" + encode(openApiKey, ENCODE_TYPE));
        urlBuilder.append("&" + encode("numOfRows", ENCODE_TYPE) + "=" + encode(contentCount, ENCODE_TYPE));
        urlBuilder.append("&" + encode("cityCode", ENCODE_TYPE) + "=" + encode(cityCode, ENCODE_TYPE));
        urlBuilder.append("&" + encode("nodeid", ENCODE_TYPE) + "=" + encode(nodeId, ENCODE_TYPE));
        urlBuilder.append("&_type=json");
        return urlBuilder.toString();
    }

    private List<CityBusRoute> extractBusRouteInfo(String jsonResponse) {
        List<CityBusRoute> result = new ArrayList<>();
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
                return gson.fromJson(item, availableCityBusType);
            }
            if (item.isJsonObject()) {
                result.add(gson.fromJson(item, CityBusRoute.class));
            }
            return result;
        } catch (JsonSyntaxException e) {
            return result;
        }
    }
}
