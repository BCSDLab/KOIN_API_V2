package in.koreatech.koin.domain.bus.util;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;

import in.koreatech.koin.domain.bus.dto.TmoneyOpenApiResponse;
import in.koreatech.koin.domain.bus.exception.BusOpenApiException;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCache;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCacheInfo;
import in.koreatech.koin.domain.bus.model.express.ExpressBusRoute;
import in.koreatech.koin.domain.bus.model.express.ExpressBusStationNode;
import in.koreatech.koin.domain.bus.model.express.TmoneyOpenApiExpressBusArrival;
import in.koreatech.koin.domain.bus.repository.ExpressBusCacheRepository;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import in.koreatech.koin.global.exception.KoinIllegalStateException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * OpenApi 상세: 티머니 Api
 * https://apiportal.tmoney.co.kr:18443/apiGallery/apiGalleryDetail.do?apiId=API201906241410183kp&apiPckgId=APK2024051316462950w&isTestYn=Y
 */
@Component
public class TmoneyExpressBusClient extends ExpressBusClient<TmoneyOpenApiResponse> {

    private static final String OPEN_API_URL = "https://apigw.tmoney.co.kr:5556/gateway/xzzIbtListGet/v1/ibt_list";

    private final String openApiKey;

    public TmoneyExpressBusClient(
        RestTemplate restTemplate,
        @Value("${OPEN_API_KEY_TMONEY}") String openApiKey,
        VersionRepository versionRepository,
        Gson gson,
        Clock clock,
        ExpressBusCacheRepository expressBusCacheRepository
    ) {
        super(versionRepository, restTemplate, gson, clock, expressBusCacheRepository);
        this.openApiKey = openApiKey;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "tmoneyExpressBus")
    public void storeRemainTimeByOpenApi() {
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

    @Override
    protected List<TmoneyOpenApiExpressBusArrival> extractBusArrivalInfo(TmoneyOpenApiResponse tmoneyResponse) {
        if (!tmoneyResponse.code().equals("00000")) {
            return Collections.emptyList();
        }
        return tmoneyResponse.response().LINE_LIST();
    }

    @Override
    protected TmoneyOpenApiResponse getOpenApiResponse(BusStation depart, BusStation arrival) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-Gateway-APIKey", openApiKey);
            headers.set("Accept", "*/*");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            UriComponents uri = getBusApiURL(depart, arrival);
            ResponseEntity<TmoneyOpenApiResponse> response = restTemplate.exchange(
                uri.toString(),
                HttpMethod.GET,
                entity,
                TmoneyOpenApiResponse.class
            );
            return response.getBody();
        } catch (Exception ignore) {
            throw BusOpenApiException.withDetail("depart: " + depart + " arrival: " + arrival);
        }
    }

    @Override
    protected UriComponents getBusApiURL(BusStation depart, BusStation arrival) {
        ExpressBusStationNode departNode = ExpressBusStationNode.from(depart);
        ExpressBusStationNode arrivalNode = ExpressBusStationNode.from(arrival);
        LocalDateTime today = LocalDateTime.now(clock);
        UriComponents uri;
        try {
            uri = UriComponentsBuilder
                .fromHttpUrl(OPEN_API_URL)
                .pathSegment(today.format(ofPattern("yyyyMMdd")))
                .pathSegment("0000")
                .pathSegment(departNode.getTmoneyStationId())
                .pathSegment(arrivalNode.getTmoneyStationId())
                .pathSegment("9")
                .pathSegment("0")
                .build();
            return uri;
        } catch (Exception e) {
            throw new KoinIllegalStateException("시외버스 API URL 생성중 문제가 발생했습니다.");
        }
    }
}
