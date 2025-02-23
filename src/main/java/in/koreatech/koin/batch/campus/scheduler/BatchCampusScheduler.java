package in.koreatech.koin.batch.campus.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.batch.campus.bus.city.service.BatchCityBusService;
import in.koreatech.koin.batch.campus.koreatech.dining.service.BatchDiningService;
import in.koreatech.koin.batch.campus.service.BatchPortalLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchCampusScheduler {

    private final BatchCityBusService batchCityBusService;
    private final BatchPortalLoginService batchPortalLoginService;
    private final BatchDiningService batchDiningService;

    @Scheduled(cron = "0 0 0 * * 0")
    public void updateCityBusTimetables() {
        batchCityBusService.update();
    }

    @Scheduled(cron = "0 0 5,15 * * *")
    @Scheduled(cron = "0 15,30 17,18 * * *")
    @Scheduled(cron = "0 */30 8-11 * * *")
    @Scheduled(cron = "0 15 11 * * *")
    public void updateDining() {
        batchPortalLoginService.login();
        batchDiningService.update();
    }
}
