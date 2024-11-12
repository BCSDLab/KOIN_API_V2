package in.koreatech.koin.domain.bus.city.util;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
import in.koreatech.koin.domain.bus.city.model.CityBusArrival;
import in.koreatech.koin.domain.bus.city.model.CityBusCache;
import in.koreatech.koin.domain.bus.city.model.CityBusCacheInfo;
import in.koreatech.koin.domain.bus.city.model.enums.BusStationNode;
import in.koreatech.koin.domain.bus.city.repository.CityBusCacheRepository;
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
    public void storeRemainTime() {
        List<List<CityBusArrival>> arrivalInfosList = new ArrayList<>();
        BusStationNode.getNodeIds().forEach((nodeId) -> {
            try {
                arrivalInfosList.add(extractBusArrivalInfo(getOpenApiResponse(nodeId)));
            } catch (BusOpenApiException ignored) {
            }
        });
        LocalDateTime updatedAt = LocalDateTime.now(clock);
        arrivalInfosList.forEach((arrivalInfos) -> cityBusCacheRepository.save(
            CityBusCache.of(
                arrivalInfos.get(0).nodeid(),
                arrivalInfos.stream()
                    .map(busArrivalInfo -> CityBusCacheInfo.of(busArrivalInfo, updatedAt))
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

    private List<CityBusArrival> extractBusArrivalInfo(CityBusApiResponse response) {
        if (!response.response().header().resultCode().equals("00")
            || response.response().body().totalCount() == 0) {
            return Collections.emptyList();
        }
        return response.response().body().items().item();
    }
}
