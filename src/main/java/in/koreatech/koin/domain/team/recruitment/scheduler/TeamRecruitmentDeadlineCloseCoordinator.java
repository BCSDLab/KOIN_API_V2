package in.koreatech.koin.domain.team.recruitment.scheduler;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamRecruitmentDeadlineCloseCoordinator {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int CANDIDATE_BATCH_SIZE = 100;
    private static final Pageable CANDIDATE_PAGE = PageRequest.of(
        0,
        CANDIDATE_BATCH_SIZE,
        Sort.by(Sort.Direction.ASC, "id")
    );

    private final TeamRecruitmentRepository recruitmentRepository;
    private final TeamRecruitmentDeadlineCloseProcessor closeProcessor;
    private final Clock clock;

    public void closeExpiredRecruitments() {
        LocalDate today = LocalDate.now(clock.withZone(KST));
        Page<TeamRecruitment> candidatePage = recruitmentRepository
            .findAllByStatusAndDeadlineDateBefore(RECRUITING, today, CANDIDATE_PAGE);
        if (candidatePage == null) {
            return;
        }

        List<Integer> candidateIds = candidatePage
            .getContent()
            .stream()
            .map(TeamRecruitment::getId)
            .filter(Objects::nonNull)
            .toList();

        for (Integer recruitmentId : candidateIds) {
            try {
                closeProcessor.closeIfExpired(recruitmentId, today);
            } catch (Exception exception) {
                log.error("팀원 모집글 마감 처리에 실패했습니다. recruitmentId={}", recruitmentId, exception);
            }
        }
    }
}
