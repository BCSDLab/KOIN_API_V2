package in.koreatech.koin.domain.bus.city.client;

import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.domain.bus.city.dto.CityBusRoute;
import in.koreatech.koin.domain.bus.city.dto.CityBusRouteApiResponse;
import in.koreatech.koin.domain.bus.city.model.enums.BusStationNode;
import in.koreatech.koin.domain.bus.city.repository.CityBusRouteCacheRepository;
import in.koreatech.koin.domain.bus.city.util.URIProvider;
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
                Set<CityBusRoute> routes = Set.copyOf(getOpenApiResponse(nodeId).extractBusRouteInfo());
                cityBusRouteCacheRepository.save(CityBusRoute.toCityBusRouteCache(nodeId, routes));
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
}
