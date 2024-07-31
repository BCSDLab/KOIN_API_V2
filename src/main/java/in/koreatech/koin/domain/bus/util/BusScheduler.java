package in.koreatech.koin.domain.bus.util;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.global.domain.callcontoller.CallController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusScheduler {

    private final List<ExpressBusClient<?, ?>> expressBusClients;
    private final CallController callController;
    private final CityBusClient cityBusClient;
    private final CityBusRouteClient cityBusRouteClient;

    @Scheduled(cron = "0 * * * * *")
    public void cacheCityBusByOpenApi() {
        try {
            cityBusClient.storeRemainTimeByOpenApi();
            cityBusRouteClient.storeCityBusRoute();
        } catch (Exception e) {
            log.warn("시내버스 스케줄링 과정에서 오류가 발생했습니다.");
        }
    }

    @Scheduled(cron = "0 30 0 * * *")
    public void cacheExpressBusByOpenApi() {
        try {
            callController.selectChildClass(expressBusClients).storeRemainTimeByOpenApi();
        } catch (Exception e) {
            log.warn("시외버스 스케줄링 과정에서 오류가 발생했습니다.");
        }
    }
}
