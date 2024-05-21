package in.koreatech.koin.domain.bus.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
        } catch (Exception e) {
            log.warn("시내버스 스케줄링 과정에서 오류가 발생했습니다.");
        }
    }

    // TODO: 시외버스 Open API 복구되면 주석 해제
/*    @Scheduled(cron = "0 0 * * * *")
    public void cacheExpressBusByOpenApi() {
        try {
            expressBusOpenApiClient.storeRemainTimeByOpenApi();
        } catch (Exception e) {
            log.warn("시외버스 스케줄링 과정에서 오류가 발생했습니다.");
        }
    }*/
}
