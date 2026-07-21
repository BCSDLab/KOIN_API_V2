package in.koreatech.koin.domain.notification.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.notification.service.NotificationDeviceTokenCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDeviceTokenCleanupScheduler {

    private final NotificationDeviceTokenCleanupService notificationDeviceTokenCleanupService;

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupUnregisteredDeviceTokens() {
        try {
            notificationDeviceTokenCleanupService.cleanupYesterdayUnregisteredDeviceTokens();
        } catch (Exception e) {
            log.warn("FCM 무효 토큰 정리 과정에서 오류가 발생했습니다.", e);
        }
    }
}
