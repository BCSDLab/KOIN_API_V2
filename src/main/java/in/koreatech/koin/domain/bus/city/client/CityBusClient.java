package in.koreatech.koin.domain.bus.city.client;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.domain.bus.city.dto.CityBusApiResponse;
import in.koreatech.koin.domain.bus.city.dto.CityBusArrival;
import in.koreatech.koin.domain.bus.city.model.CityBusCache;
import in.koreatech.koin.domain.bus.city.model.enums.BusStationNode;
import in.koreatech.koin.domain.bus.city.repository.CityBusCacheRepository;
import in.koreatech.koin.domain.bus.city.util.URIProvider;
import in.koreatech.koin.domain.bus.global.exception.BusOpenApiException;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스도착정보
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098530
 */
@Component
@RequiredArgsConstructor
public class CityBusClient {

    private static final String OPEN_API_URL = "https://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList";

    private final VersionRepository versionRepository;
    private final CityBusCacheRepository cityBusCacheRepository;
    private final RestTemplate restTemplate;
    private final URIProvider uriProvider;
    private final Clock clock;

    @Transactional
    @CircuitBreaker(name = "cityBus")
    // TODO: 가독성 개선
    public void storeRemainTime() {
        List<List<CityBusArrival>> arrivalInfosList = new ArrayList<>();
        BusStationNode.getNodeIds().forEach((nodeId) -> {
            try {
                arrivalInfosList.add(getOpenApiResponse(nodeId).extractBusArrivalInfo());
            } catch (BusOpenApiException ignored) {
            }
        });
        LocalDateTime updatedAt = LocalDateTime.now(clock);
        arrivalInfosList.forEach((arrivalInfos) -> cityBusCacheRepository.save(
            CityBusCache.of(
                arrivalInfos.get(0).nodeid(),
                arrivalInfos.stream()
                    .map(busArrivalInfo -> busArrivalInfo.toCityBusCacheInfo(updatedAt))
                    .toList()
            )
        ));
        versionRepository.getByType(VersionType.CITY).update(clock);
    }

    private CityBusApiResponse getOpenApiResponse(String nodeId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "*/*");
            ResponseEntity<CityBusApiResponse> response = restTemplate.exchange(
                uriProvider.getRequestURI(OPEN_API_URL, nodeId, "30"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                CityBusApiResponse.class
            );
            return response.getBody();
        } catch (Exception ignored) {
            throw BusOpenApiException.withDetail("nodeId : " + nodeId);
        }
    }
}
