package in.koreatech.koin.batch.campus.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.batch.campus.bus.city.service.BatchCityBusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchCampusScheduler {

    private final BatchCityBusService batchCityBusService;

    @Scheduled(cron = "0 0 0 * * 0")
    public void updateCityBusTimetables() {
        batchCityBusService.update();
    }
}
