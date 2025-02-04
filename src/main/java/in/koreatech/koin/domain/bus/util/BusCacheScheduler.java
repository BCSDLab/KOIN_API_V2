package in.koreatech.koin.domain.bus.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusCacheScheduler {

    private final BusCacheService busSchedulerService;

    // TODO: 레거시 - 버스번호는 정적으로 유지함
    @Scheduled(cron = "0 * * * * *")
    public void cacheCityBusByOpenApi() {
        try {
            busSchedulerService.cacheCityBusByOpenApi();
        } catch (Exception e) {
            log.warn("시내버스 스케줄링 과정에서 오류가 발생했습니다.");
        }
    }

    @Scheduled(cron = "0 30 0 * * *")
    public void cacheExpressBusByOpenApi() {
        try {
            busSchedulerService.storeRemainTimeByRatio();
        } catch (Exception e) {
            log.warn("시외버스 스케줄링 과정에서 오류가 발생했습니다.");
        }
    }
}
