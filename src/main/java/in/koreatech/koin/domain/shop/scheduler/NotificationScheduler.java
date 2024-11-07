package in.koreatech.koin.domain.shop.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.shop.service.NotificationScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationScheduleService notificationScheduleService;

    @Scheduled(cron = "0 * * * * *")
    public void sendDueNotifications() {
        try {
            notificationScheduleService.sendDueNotifications();
        } catch (Exception e) {
            log.warn("리뷰유도 알림 전송 과정에서 오류가 발생했습니다.");
        }
    }
}
