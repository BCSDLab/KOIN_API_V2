package in.koreatech.koin.domain.team.recruitment.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "team-recruitment.outbox",
    name = "enabled",
    havingValue = "true"
)
public class TeamRecruitmentOutboxScheduler {

    private final TeamRecruitmentOutboxWorker worker;

    @Scheduled(fixedDelayString = "${team-recruitment.outbox.fixed-delay-ms:10000}")
    public void publishTeamRecruitmentOutbox() {
        try {
            worker.processBatch();
        } catch (Exception exception) {
            log.error("팀원 모집 outbox 스케줄러 처리 중 오류가 발생했습니다.", exception);
        }
    }
}
