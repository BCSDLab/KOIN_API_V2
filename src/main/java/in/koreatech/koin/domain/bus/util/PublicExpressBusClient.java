package in.koreatech.koin.domain.bus.util;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;

import in.koreatech.koin.domain.bus.dto.PublicOpenApiResponse;
import in.koreatech.koin.domain.bus.exception.BusOpenApiException;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCache;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCacheInfo;
import in.koreatech.koin.domain.bus.model.express.ExpressBusRoute;
import in.koreatech.koin.domain.bus.model.express.ExpressBusStationNode;
import in.koreatech.koin.domain.bus.model.express.PublicOpenApiExpressBusArrival;
import in.koreatech.koin.domain.bus.repository.ExpressBusCacheRepository;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import in.koreatech.koin.global.exception.KoinIllegalStateException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스도착정보
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098541
 */
@Component
public class PublicExpressBusClient extends ExpressBusClient<PublicOpenApiResponse> {

    private static final String OPEN_API_URL = "https://apis.data.go.kr/1613000/SuburbsBusInfoService/getStrtpntAlocFndSuberbsBusInfo";

    private final String openApiKey;

    public PublicExpressBusClient(
        @Value("${OPEN_API_KEY_PUBLIC}") String openApiKey,
        VersionRepository versionRepository,
        RestTemplate restTemplate,
        Gson gson,
        Clock clock,
        ExpressBusCacheRepository expressBusCacheRepository
    ) {
        super(versionRepository, restTemplate, gson, clock, expressBusCacheRepository);
        this.openApiKey = openApiKey;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "publicExpressBus")
    public void storeRemainTimeByOpenApi() {
        for (BusStation depart : BusStation.values()) {
            for (BusStation arrival : BusStation.values()) {
                if (depart == arrival || depart.equals(BusStation.STATION) || arrival.equals(BusStation.STATION)) {
                    continue;
                }
                PublicOpenApiResponse openApiResponse;
                try {
                    openApiResponse = getOpenApiResponse(depart, arrival);
                } catch (BusOpenApiException e) {
                    continue;
                }
                List<PublicOpenApiExpressBusArrival> busArrivals = extractBusArrivalInfo(openApiResponse);

                ExpressBusCache expressBusCache = ExpressBusCache.of(
                    new ExpressBusRoute(depart.getName(), arrival.getName()),
                    // API로 받은 yyyyMMddHHmm 형태의 시간을 HH:mm 형태로 변환하여 Redis에 저장한다.
                    busArrivals.stream()
                        .map(it -> new ExpressBusCacheInfo(
                            LocalTime.parse(
                                LocalDateTime.parse(it.depPlandTime(), ofPattern("yyyyMMddHHmm"))
                                    .format(ofPattern("HH:mm"))
                            ),
                            LocalTime.parse(
                                LocalDateTime.parse(it.arrPlandTime(), ofPattern("yyyyMMddHHmm"))
                                    .format(ofPattern("HH:mm"))
                            ),
                            it.charge()
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
    protected PublicOpenApiResponse getOpenApiResponse(BusStation depart, BusStation arrival) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "*/*");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            UriComponents uri = getBusApiURL(depart, arrival);
            ResponseEntity<PublicOpenApiResponse> response = restTemplate.exchange(
                uri.toString(),
                HttpMethod.GET,
                entity,
                PublicOpenApiResponse.class
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
        LocalDateTime yesterday = LocalDateTime.now(clock).minusDays(1);
        UriComponents uri;
        try {
            uri = UriComponentsBuilder
                .fromHttpUrl(OPEN_API_URL)
                .queryParam("serviceKey", encode(openApiKey, UTF_8))
                .queryParam("numOfRows", 30)
                .queryParam("_type", "json")
                .queryParam("depTerminalId", departNode.getStationId())
                .queryParam("arrTerminalId", arrivalNode.getStationId())
                .queryParam("depPlandTime", yesterday.format(ofPattern("yyyyMMdd")))
                .build();
            return uri;
        } catch (Exception e) {
            throw new KoinIllegalStateException("시외버스 API URL 생성중 문제가 발생했습니다.");
        }
    }

    @Override
    protected List<PublicOpenApiExpressBusArrival> extractBusArrivalInfo(PublicOpenApiResponse publicResponse) {
        if (!publicResponse.response().header().resultCode().equals("00")
            || publicResponse.response().body().totalCount() == 0) {
            return Collections.emptyList();
        }
        return publicResponse.response().body().items().item();
    }
}
