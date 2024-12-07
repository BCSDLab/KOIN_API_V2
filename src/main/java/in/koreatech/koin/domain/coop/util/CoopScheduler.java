package in.koreatech.koin.domain.coop.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.coop.service.CoopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoopScheduler {

    private final CoopService coopService;

    @Scheduled(cron = "0 0/6 7 * * *")
    @Scheduled(cron = "0 30/6 10-11 * * *")
    @Scheduled(cron = "0 30/6 16-17 * * *")
    public void notifyDiningImageUpload() {
        try {
            coopService.sendDiningNotify();
        } catch (Exception e) {
            log.warn("식단 이미지 알림 과정에서 오류가 발생했습니다.");
        }
    }
}
