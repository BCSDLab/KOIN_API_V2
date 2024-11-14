package in.koreatech.koin.domain.bus.client;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.domain.bus.dto.city.CityBusApiResponse;
import in.koreatech.koin.domain.bus.dto.city.CityBusArrival;
import in.koreatech.koin.domain.bus.exception.BusOpenApiException;
import in.koreatech.koin.domain.bus.exception.MalformedApiUriException;
import in.koreatech.koin.domain.bus.model.city.CityBusCache;
import in.koreatech.koin.domain.bus.model.enums.BusStationNode;
import in.koreatech.koin.domain.bus.repository.CityBusCacheRepository;
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

    private static final String CHEONAN_CITY_CODE = "34010";
    private static final String OPEN_API_URL = "https://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList";

    private final RestTemplate restTemplate;

    @Value("${OPEN_API_KEY_PUBLIC}")
    private String openApiKey;

    @CircuitBreaker(name = "cityBus")
    public CityBusApiResponse getOpenApiResponse(String nodeId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "*/*");
            ResponseEntity<CityBusApiResponse> response = restTemplate.exchange(
                getRequestURI(nodeId),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                CityBusApiResponse.class
            );
            return response.getBody();
        } catch (Exception ignored) {
            throw BusOpenApiException.withDetail("nodeId : " + nodeId);
        }
    }

    private URI getRequestURI(String nodeId) {
        String uriString = MessageFormat.format(
            "{0}?serviceKey={1}&numOfRows={2}&cityCode={3}&nodeId={4}&_type=json",
            OPEN_API_URL,
            encode(openApiKey, UTF_8),
            encode("30", UTF_8),
            encode(CHEONAN_CITY_CODE, UTF_8),
            encode(nodeId, UTF_8)
        );
        try {
            return new URI(uriString);
        } catch (Exception e) {
            throw MalformedApiUriException.withDetail("uri: " + uriString);
        }
    }
}
