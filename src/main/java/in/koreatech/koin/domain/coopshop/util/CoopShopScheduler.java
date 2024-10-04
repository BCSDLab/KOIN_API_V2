package in.koreatech.koin.domain.coopshop.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CoopShopScheduler {

    private final CoopShopService coopShopService;

    @Scheduled(cron = "0 5 0 * * *")
    public void checkSemesterForUpdate() {
        coopShopService.updateSemester();
    }
}
