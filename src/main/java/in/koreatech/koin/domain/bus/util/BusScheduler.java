package in.koreatech.koin.domain.bus.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BusScheduler {

    private final CityBusOpenApiClient cityBusOpenApiClient;
    private final ExpressBusOpenApiClient expressBusOpenApiClient;

    @Scheduled(cron = "0 */1 * * * *")
    public void cacheCityBusByOpenApi() {
        cityBusOpenApiClient.storeRemainTimeByOpenApi();
    }

    // 시외버스 Open API 복구되면 주석 해제
/*    @Scheduled(cron = "0 0 * * * *")
    public void cacheExpressBusByOpenApi() {
        expressBusOpenApiClient.storeRemainTimeByOpenApi();
    }*/
}
