package in.koreatech.koin.domain.club.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.club.scheduler.service.ClubScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClubScheduler {

    private final ClubScheduleService scheduleService;

    @Scheduled(cron = "0 0 0 * * MON")
    public void updateHotClub() {
        try {
            scheduleService.updateHotClub();
        } catch (Exception e) {
            log.error("인기 동아리 업데이트 중에 오류가 발생했습니다.", e);
        }
    }

    @Scheduled(fixedRate = 600000)
    public void syncClubHits() {
        try {
            scheduleService.syncHitsFromRedisToDatabase();
        } catch (Exception e) {
            log.error("조회수 동기화 중에 오류가 발생했습니다.", e);
        }
    }

    @Scheduled(cron = "0 30 12 * * *")
    public void sendClubEventNotificationsBeforeOneDay() {
        try {
            scheduleService.sendClubEventNotificationsBeforeOneDay();
        } catch (Exception e) {
            log.error("동아리 이벤트 하루 전 알림에 오류가 발생했습니다.", e);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void sendClubEventNotificationBeforeOneHour() {
        try {
            scheduleService.sendClubEventNotificationsBeforeOneHour();
        } catch (Exception e) {
            log.error("동아리 이벤트 1시간 전 알림에 오류가 발생했습니다.", e);
        }
    }
}
