package in.koreatech.koin.domain.bus.service.city.client;

import static java.net.URLEncoder.encode;

import java.net.URL;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.domain.bus.service.city.dto.CityBusApiResponse;
import in.koreatech.koin.domain.bus.exception.BusOpenApiException;
import in.koreatech.koin.domain.bus.service.city.model.CityBusArrival;
import in.koreatech.koin.domain.bus.service.city.model.CityBusCache;
import in.koreatech.koin.domain.bus.service.city.model.CityBusCacheInfo;
import in.koreatech.koin.domain.bus.service.city.model.CityBusRemainTime;
import in.koreatech.koin.domain.bus.enums.BusStationNode;
import in.koreatech.koin.domain.bus.service.city.repository.CityBusCacheRepository;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스도착정보
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098530
 */
@Component
@Transactional(readOnly = true)
public class CityBusClient {

    private static final String OPEN_API_URL = "https://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList";
    private static final String ENCODE_TYPE = "UTF-8";
    private static final String CHEONAN_CITY_CODE = "34010";

    private final String openApiKey;
    private final Clock clock;
    private final VersionRepository versionRepository;
    private final CityBusCacheRepository cityBusCacheRepository;
    private final RestTemplate restTemplate;

    public CityBusClient(
        @Value("${OPEN_API_KEY_PUBLIC}") String openApiKey,
        Clock clock,
        VersionRepository versionRepository,
        CityBusCacheRepository cityBusCacheRepository,
        RestTemplate restTemplate
    ) {
        this.openApiKey = openApiKey;
        this.clock = clock;
        this.versionRepository = versionRepository;
        this.cityBusCacheRepository = cityBusCacheRepository;
        this.restTemplate = restTemplate;
    }

    public List<CityBusRemainTime> getBusRemainTime(List<String> nodeIds) {
        List<CityBusRemainTime> result = new ArrayList<>();
        nodeIds.forEach(nodeId -> {
            Optional<CityBusCache> cityBusCache = cityBusCacheRepository.findById(nodeId);
            if (cityBusCache.isPresent()) {
                result.addAll(
                    cityBusCache.map(busCache -> busCache.getBusInfos().stream().map(CityBusRemainTime::from).toList())
                        .get());
            }
        });
        return result;
    }

    @Transactional
    @CircuitBreaker(name = "cityBus")
    public void storeRemainTime() {
        List<List<CityBusArrival>> arrivalInfosList = new ArrayList<>();
        List<String> nodeIds = BusStationNode.getNodeIds();
        for (String nodeId : nodeIds) {
            try {
                List<CityBusArrival> busArrivalInfo = extractBusArrivalInfo(getOpenApiResponse(nodeId));
                arrivalInfosList.add(busArrivalInfo);
            } catch (BusOpenApiException e) {
                continue;
            }
        }

        LocalDateTime updatedAt = LocalDateTime.now(clock);

        for (List<CityBusArrival> arrivalInfos : arrivalInfosList) {
            cityBusCacheRepository.save(
                CityBusCache.of(
                    arrivalInfos.get(0).nodeid(),
                    arrivalInfos.stream()
                        .map(busArrivalInfo -> CityBusCacheInfo.of(busArrivalInfo, updatedAt))
                        .toList()
                )
            );
        }

        versionRepository.getByType(VersionType.CITY).update(clock);
    }

    public CityBusApiResponse getOpenApiResponse(String nodeId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "*/*");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            URL uri = new URL(getRequestURL(CHEONAN_CITY_CODE, nodeId));
            ResponseEntity<CityBusApiResponse> response = restTemplate.exchange(
                uri.toURI(),
                HttpMethod.GET,
                entity,
                CityBusApiResponse.class
            );
            return response.getBody();
        } catch (Exception ignore) {
            throw BusOpenApiException.withDetail("nodeId : " + nodeId);
        }
    }

    private String getRequestURL(String cityCode, String nodeId) {
        String contentCount = "30";
        StringBuilder urlBuilder = new StringBuilder(OPEN_API_URL);
        try {
            urlBuilder.append("?" + encode("serviceKey", ENCODE_TYPE) + "=" + encode(openApiKey, ENCODE_TYPE));
            urlBuilder.append("&" + encode("numOfRows", ENCODE_TYPE) + "=" + encode(contentCount, ENCODE_TYPE));
            urlBuilder.append("&" + encode("cityCode", ENCODE_TYPE) + "=" + encode(cityCode, ENCODE_TYPE));
            urlBuilder.append("&" + encode("nodeId", ENCODE_TYPE) + "=" + encode(nodeId, ENCODE_TYPE));
            urlBuilder.append("&_type=json");
            return urlBuilder.toString();
        } catch (Exception e) {
            throw new KoinIllegalStateException("시내버스 도착정보 API URL 생성중 문제가 발생했습니다.", "uri:" + urlBuilder);
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
