package in.koreatech.koin.domain.club.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.club.service.ClubScheduleService;
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
}
