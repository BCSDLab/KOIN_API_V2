package in.koreatech.koin.domain.bus.batch.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.batch.service.CityBusCacheService;
import in.koreatech.koin.domain.bus.batch.service.ExpressBusCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusScheduler {

    private final CityBusCacheService cityBusCacheService;
    private final ExpressBusCacheService expressBusCacheService;

    @Scheduled(cron = "0 * * * * *")
    public void cacheCityBusByOpenApi() {
        try {
            cityBusCacheService.cacheRemainTime();
            cityBusCacheService.cacheCityBusRoute();
        } catch (Exception e) {
            log.warn("시내버스 스케줄링 과정에서 오류가 발생했습니다.");
        }
    }

    @Scheduled(cron = "0 30 0 * * *")
    public void cacheExpressBusByOpenApi() {
        try {
            expressBusCacheService.cacheRemainTimeByRatio();
        } catch (Exception e) {
            log.warn("시외버스 스케줄링 과정에서 오류가 발생했습니다.");
        }
    }
}
