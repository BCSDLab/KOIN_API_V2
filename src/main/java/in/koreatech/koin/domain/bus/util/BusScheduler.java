package in.koreatech.koin.domain.bus.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.exception.BusOpenApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusScheduler {

    private final CityBusOpenApiClient cityBusOpenApiClient;
    private final ExpressBusOpenApiClient expressBusOpenApiClient;

    @Scheduled(cron = "0 */1 * * * *")
    public void cacheCityBusByOpenApi() {
        try {
            cityBusOpenApiClient.storeRemainTimeByOpenApi();
        } catch (BusOpenApiException e) {
            log.warn("버스 Open API 응답이 정상적이지 않습니다.");
        }
    }

    // TODO: 시외버스 Open API 복구되면 주석 해제
/*    @Scheduled(cron = "0 0 * * * *")
    public void cacheExpressBusByOpenApi() {
        try {
            expressBusOpenApiClient.storeRemainTimeByOpenApi();
        } catch (BusOpenApiException e) {
            log.warn("버스 Open API 응답이 정상적이지 않습니다.");
        }
    }*/
}
