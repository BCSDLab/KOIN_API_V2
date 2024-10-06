package in.koreatech.koin.domain.coopshop.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoopShopScheduler {

    private final CoopShopService coopShopService;

    @Scheduled(cron = "0 5 0 * * *")
    public void checkSemesterForUpdate() {
        try {
            coopShopService.updateSemester();    
        } catch(Exception e) {
            log.warn("생협 운영시간 스케줄링 과정에서 오류가 발생했습니다.");
        }
    }
}
