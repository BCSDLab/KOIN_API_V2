package in.koreatech.koin.domain.team.recruitment.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamRecruitmentDeadlineScheduler {

    private final TeamRecruitmentDeadlineCloseCoordinator deadlineCloseCoordinator;

    @Scheduled(fixedDelayString = "${team-recruitment.deadline-scheduler.fixed-delay-ms:60000}")
    public void closeExpiredRecruitments() {
        try {
            deadlineCloseCoordinator.closeExpiredRecruitments();
        } catch (Exception exception) {
            log.error("팀원 모집 마감 스케줄러 처리 중 오류가 발생했습니다.", exception);
        }
    }
}
