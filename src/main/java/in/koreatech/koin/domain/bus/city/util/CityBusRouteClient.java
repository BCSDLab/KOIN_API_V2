package in.koreatech.koin.domain.bus.city.util;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.domain.bus.city.dto.CityBusRouteApiResponse;
import in.koreatech.koin.domain.bus.city.model.CityBusRoute;
import in.koreatech.koin.domain.bus.city.model.CityBusRouteCache;
import in.koreatech.koin.domain.bus.city.model.enums.BusStationNode;
import in.koreatech.koin.domain.bus.city.repository.CityBusRouteCacheRepository;
import in.koreatech.koin.domain.bus.global.exception.BusOpenApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스정류소정보 - 정류소별경유노선 목록조회
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098534
 */
@Component
@RequiredArgsConstructor
public class CityBusRouteClient {

    private static final String OPEN_API_URL = "https://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnThrghRouteList";

    private final CityBusRouteCacheRepository cityBusRouteCacheRepository;
    private final RestTemplate restTemplate;
    private final URIProvider uriProvider;

    @Transactional
    @CircuitBreaker(name = "cityBusRoute")
    public void storeCityBusRoute() {
        BusStationNode.getNodeIds().forEach((nodeId) -> {
            try {
                Set<CityBusRoute> routes = Set.copyOf(extractBusRouteInfo(getOpenApiResponse(nodeId)));
                cityBusRouteCacheRepository.save(CityBusRouteCache.of(nodeId, routes));
            } catch (BusOpenApiException ignored) {
            }
        });
    }

    private CityBusRouteApiResponse getOpenApiResponse(String nodeId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "*/*");
            ResponseEntity<CityBusRouteApiResponse> response = restTemplate.exchange(
                uriProvider.getRequestURI(OPEN_API_URL, nodeId, "50"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                CityBusRouteApiResponse.class
            );
            return response.getBody();
        } catch (Exception ignored) {
            throw BusOpenApiException.withDetail("nodeId: " + nodeId);
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
