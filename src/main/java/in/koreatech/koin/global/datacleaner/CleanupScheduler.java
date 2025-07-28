package in.koreatech.koin.global.datacleaner;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupScheduler {

    private final CleanupService cleanUpService;

    @Scheduled(cron = "0 0 1 1 * *")
    public void cleanupSoftDeletedData() {
        try {
            cleanUpService.cleanupSoftDeletedData();
        } catch (Exception e) {
            log.warn("데이터 정리 스케줄링 과정에서 오류가 발생했습니다.");
        }
    }
}
