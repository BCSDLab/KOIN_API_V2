package in.koreatech.koin.domain.bus.service.express.client;

import static java.net.URLEncoder.encode;
import static java.time.format.DateTimeFormatter.ofPattern;

import java.net.URL;
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

import in.koreatech.koin.domain.bus.service.express.dto.PublicOpenApiResponse;
import in.koreatech.koin.domain.bus.exception.BusOpenApiException;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusCache;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusCacheInfo;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusRoute;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusStationNode;
import in.koreatech.koin.domain.bus.service.express.model.PublicOpenApiExpressBusArrival;
import in.koreatech.koin.domain.bus.service.express.ExpressBusCacheRepository;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import in.koreatech.koin.global.apiloadbalancer.ApiLoadBalance;
import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스도착정보
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098541
 */
@Component
@ApiLoadBalance(ratio = 2)
public class PublicExpressBusClient extends ExpressBusClient {

    private static final String OPEN_API_URL = "https://apis.data.go.kr/1613000/SuburbsBusInfoService/getStrtpntAlocFndSuberbsBusInfo";
    private static final String ENCODE_TYPE = "UTF-8";

    private final RestTemplate restTemplate;
    private final String openApiKey;

    public PublicExpressBusClient(
        @Value("${OPEN_API_KEY_PUBLIC}") String openApiKey,
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
    @CircuitBreaker(name = "publicExpressBus")
    public void storeRemainTime() {
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

    private PublicOpenApiResponse getOpenApiResponse(BusStation depart, BusStation arrival) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "*/*");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            URL uri = new URL(getRequestURL(depart, arrival));
            ResponseEntity<PublicOpenApiResponse> response = restTemplate.exchange(
                uri.toURI(),
                HttpMethod.GET,
                entity,
                PublicOpenApiResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode(),
                "시외버스 Api 호출 중 문제가 발생했습니다. message: " + e.getMessage());
        } catch (Exception ignore) {
            throw BusOpenApiException.withDetail("depart: " + depart + " arrival: " + arrival);
        }
    }

    private String getRequestURL(BusStation depart, BusStation arrival) {
        ExpressBusStationNode departNode = ExpressBusStationNode.from(depart);
        ExpressBusStationNode arrivalNode = ExpressBusStationNode.from(arrival);
        StringBuilder urlBuilder = new StringBuilder(OPEN_API_URL); /*URL*/
        try {
            urlBuilder.append("?" + encode("serviceKey", ENCODE_TYPE) + "=" + encode(openApiKey, ENCODE_TYPE));
            urlBuilder.append("&" + encode("numOfRows", ENCODE_TYPE) + "=" + encode("30", ENCODE_TYPE));
            urlBuilder.append("&" + encode("_type", ENCODE_TYPE) + "=" + encode("json", ENCODE_TYPE));
            urlBuilder.append(
                "&" + encode("depTerminalId", ENCODE_TYPE) + "=" + encode(departNode.getStationId(), ENCODE_TYPE));
            urlBuilder.append(
                "&" + encode("arrTerminalId", ENCODE_TYPE) + "=" + encode(arrivalNode.getStationId(), ENCODE_TYPE));
            urlBuilder.append("&" + encode("depPlandTime", ENCODE_TYPE) + "="
                + encode(LocalDateTime.now(clock).format(ofPattern("yyyyMMdd")), ENCODE_TYPE));
            return urlBuilder.toString();
        } catch (Exception e) {
            throw new KoinIllegalStateException("공공데이터포털 시외버스 API URL 생성중 문제가 발생했습니다.", "uri:" + urlBuilder);
        }
    }

    private List<PublicOpenApiExpressBusArrival> extractBusArrivalInfo(PublicOpenApiResponse publicResponse) {
        if (!publicResponse.response().header().resultCode().equals("00")
            || publicResponse.response().body().totalCount() == 0) {
            return Collections.emptyList();
        }
        return publicResponse.response().body().items().item();
    }
}
