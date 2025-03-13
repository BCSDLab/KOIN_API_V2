package in.koreatech.koin.domain.bus.service.city.client;

import static java.net.URLEncoder.encode;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.domain.bus.service.city.dto.CityBusRouteApiResponse;
import in.koreatech.koin.domain.bus.exception.BusOpenApiException;
import in.koreatech.koin.domain.bus.service.city.model.CityBusRoute;
import in.koreatech.koin.domain.bus.service.city.model.CityBusRouteCache;
import in.koreatech.koin.domain.bus.enums.BusStationNode;
import in.koreatech.koin.domain.bus.service.city.repository.CityBusRouteCacheRepository;
import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스정류소정보 - 정류소별경유노선 목록조회
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098534
 */
@Component
@Transactional(readOnly = true)
public class CityBusRouteClient {

    private static final Set<Long> AVAILABLE_CITY_BUS = Set.of(400L, 402L, 405L);

    private static final String OPEN_API_URL = "https://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnThrghRouteList";
    private static final String ENCODE_TYPE = "UTF-8";
    private static final String CHEONAN_CITY_CODE = "34010";

    private final String openApiKey;
    private final CityBusRouteCacheRepository cityBusRouteCacheRepository;
    private final RestTemplate restTemplate;

    public CityBusRouteClient(
        @Value("${OPEN_API_KEY_PUBLIC}") String openApiKey,
        CityBusRouteCacheRepository cityBusRouteCacheRepository,
        RestTemplate restTemplate
    ) {
        this.openApiKey = openApiKey;
        this.cityBusRouteCacheRepository = cityBusRouteCacheRepository;
        this.restTemplate = restTemplate;
    }

    public Set<Long> getAvailableCityBus(List<String> nodeIds) {
        Set<Long> busNumbers = new HashSet<>();
        nodeIds.forEach(nodeId -> {
            Optional<CityBusRouteCache> routeCache = cityBusRouteCacheRepository.findById(nodeId);
            routeCache.ifPresent(cityBusRouteCache -> busNumbers.addAll(cityBusRouteCache.getBusNumbers()));
        });

        if (busNumbers.isEmpty()) {
            return new HashSet<>(AVAILABLE_CITY_BUS);
        }

        return busNumbers;
    }

    @Transactional
    @CircuitBreaker(name = "cityBusRoute")
    public void storeCityBusRoute() {
        List<String> nodeIds = BusStationNode.getNodeIds();

        for (String node : nodeIds) {
            try {
                Set<CityBusRoute> routes = Set.copyOf(extractBusRouteInfo(getOpenApiResponse(node)));
                cityBusRouteCacheRepository.save(CityBusRouteCache.of(node, routes));
            } catch (BusOpenApiException e) {
                continue;
            }
        }
    }

    public CityBusRouteApiResponse getOpenApiResponse(String nodeId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "*/*");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            URL url = new URL(getRequestURL(CHEONAN_CITY_CODE, nodeId));
            ResponseEntity<CityBusRouteApiResponse> response = restTemplate.exchange(
                url.toURI(),
                HttpMethod.GET,
                entity,
                CityBusRouteApiResponse.class
            );
            return response.getBody();
        } catch (Exception ignore) {
            throw BusOpenApiException.withDetail("nodeId : " + nodeId);
        }
    }

    private String getRequestURL(String cityCode, String nodeId) {
        String contentCount = "50";
        StringBuilder urlBuilder = new StringBuilder(OPEN_API_URL);
        try {
            urlBuilder.append("?" + encode("serviceKey", ENCODE_TYPE) + "=" + encode(openApiKey, ENCODE_TYPE));
            urlBuilder.append("&" + encode("numOfRows", ENCODE_TYPE) + "=" + encode(contentCount, ENCODE_TYPE));
            urlBuilder.append("&" + encode("cityCode", ENCODE_TYPE) + "=" + encode(cityCode, ENCODE_TYPE));
            urlBuilder.append("&" + encode("nodeid", ENCODE_TYPE) + "=" + encode(nodeId, ENCODE_TYPE));
            urlBuilder.append("&_type=json");
            return urlBuilder.toString();
        } catch (Exception e) {
            throw new KoinIllegalStateException("시내버스 경유지 API URL 생성중 문제가 발생했습니다.", "uri:" + urlBuilder);
        }
    }

    private List<CityBusRoute> extractBusRouteInfo(CityBusRouteApiResponse response) {
        if (!response.response().header().resultCode().equals("00")
            || response.response().body().totalCount() == 0) {
            return Collections.emptyList();
        }
        return response.response().body().items().item();
    }
}
