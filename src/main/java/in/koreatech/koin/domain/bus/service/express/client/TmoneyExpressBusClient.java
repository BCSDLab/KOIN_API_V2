package in.koreatech.koin.domain.bus.service.express.client;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ofPattern;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import in.koreatech.koin.domain.bus.service.express.dto.TmoneyOpenApiResponse;
import in.koreatech.koin.domain.bus.exception.BusOpenApiException;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusCache;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusCacheInfo;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusRoute;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusStationNode;
import in.koreatech.koin.domain.bus.service.express.model.TmoneyOpenApiExpressBusArrival;
import in.koreatech.koin.domain.bus.service.express.ExpressBusCacheRepository;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import in.koreatech.koin._common.apiloadbalancer.ApiLoadBalance;
import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * OpenApi 상세: 티머니 Api
 * https://apiportal.tmoney.co.kr:18443/apiGallery/apiGalleryDetail.do?apiId=API201906241410183kp&apiPckgId=APK2024051316462950w&isTestYn=Y
 */
@Component
@ApiLoadBalance(ratio = 9)
public class TmoneyExpressBusClient extends ExpressBusClient {

    private static final String OPEN_API_URL = "https://apigw.tmoney.co.kr:5556/gateway/xzzIbtListGet/v1/ibt_list";

    private final RestTemplate restTemplate;
    private final String openApiKey;

    public TmoneyExpressBusClient(
        @Value("${OPEN_API_KEY_TMONEY}") String openApiKey,
        VersionRepository versionRepository,
        RestTemplate restTemplate,
        Clock clock,
        ExpressBusCacheRepository expressBusCacheRepository
    ) {
        super(expressBusCacheRepository, versionRepository, clock);
        this.restTemplate = restTemplate;
        this.openApiKey = openApiKey;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @CircuitBreaker(name = "tmoneyExpressBus")
    public void storeRemainTime() {
        for (BusStation depart : BusStation.values()) {
            for (BusStation arrival : BusStation.values()) {
                if (depart == arrival || depart.equals(BusStation.STATION) || arrival.equals(BusStation.STATION)) {
                    continue;
                }
                TmoneyOpenApiResponse openApiResponse;
                try {
                    openApiResponse = getOpenApiResponse(depart, arrival);
                } catch (BusOpenApiException e) {
                    continue;
                }
                List<TmoneyOpenApiExpressBusArrival> busArrivals = extractBusArrivalInfo(openApiResponse);

                ExpressBusCache expressBusCache = ExpressBusCache.of(
                    new ExpressBusRoute(depart.getName(), arrival.getName()),
                    // API로 받은 HHmm 형태의 시간을 HH:mm 형태로 변환하여 Redis에 저장한다.
                    busArrivals.stream()
                        .map(it -> new ExpressBusCacheInfo(
                            LocalTime.parse(it.TIM_TIM_O(), ofPattern("HHmm")),
                            LocalTime.parse(it.TIM_TIM_O(), ofPattern("HHmm")).plusMinutes(it.LIN_TIM()),
                            1900
                        ))
                        .toList()
                );
                if (!expressBusCache.getBusInfos().isEmpty()) {
                    expressBusCacheRepository.save(expressBusCache);
                }
            }
        }
        versionRepository.getByType(VersionType.EXPRESS).update(clock);
    }

    private List<TmoneyOpenApiExpressBusArrival> extractBusArrivalInfo(TmoneyOpenApiResponse tmoneyResponse) {
        if (!tmoneyResponse.code().equals("00000")) {
            return Collections.emptyList();
        }
        return tmoneyResponse.response().LINE_LIST();
    }

    private TmoneyOpenApiResponse getOpenApiResponse(BusStation depart, BusStation arrival) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-Gateway-APIKey", openApiKey);
            headers.set("Accept", "*/*");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            UriComponents uri = getRequestURL(depart, arrival);
            ResponseEntity<TmoneyOpenApiResponse> response = restTemplate.exchange(
                uri.toString(),
                HttpMethod.GET,
                entity,
                TmoneyOpenApiResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode(),
                "시외버스 Api 호출 중 문제가 발생했습니다. message: " + e.getMessage());
        } catch (Exception ignore) {
            throw BusOpenApiException.withDetail("depart: " + depart + " arrival: " + arrival);
        }
    }

    private UriComponents getRequestURL(BusStation depart, BusStation arrival) {
        ExpressBusStationNode departNode = ExpressBusStationNode.from(depart);
        ExpressBusStationNode arrivalNode = ExpressBusStationNode.from(arrival);
        LocalDateTime today = LocalDateTime.now(clock);
        UriComponents uri;
        try {
            uri = UriComponentsBuilder
                .fromHttpUrl(OPEN_API_URL)
                .pathSegment(encode(today.format(ofPattern("yyyyMMdd")), UTF_8))
                .pathSegment("0000")
                .pathSegment(encode(departNode.getTmoneyStationId(), UTF_8))
                .pathSegment(encode(arrivalNode.getTmoneyStationId(), UTF_8))
                .pathSegment("9")
                .pathSegment("0")
                .build();
            return uri;
        } catch (Exception e) {
            throw new KoinIllegalStateException("티머니 시외버스 API URL 생성중 문제가 발생했습니다. uri: " + OPEN_API_URL);
        }
    }
}
