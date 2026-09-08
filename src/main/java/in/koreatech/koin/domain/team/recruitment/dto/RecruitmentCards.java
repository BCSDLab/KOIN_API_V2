package in.koreatech.koin.domain.team.recruitment.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;

/**
 * 모집글 카드 응답 조립. 목록/상세/내가 작성한 목록이 공유한다.
 */
public final class RecruitmentCards {

    private RecruitmentCards() {
    }

    public static RecruitmentCard of(TeamRecruitment recruitment, LocalDate today) {
        boolean recruitmentClosed = isRecruitmentClosed(recruitment, today);
        return new RecruitmentCard(
            recruitment.getId(),
            recruitment.getCategory(),
            recruitment.getTitle(),
            recruitment.getMeetingType(),
            recruitment.getActivityStartDate(),
            recruitment.getActivityEndDate(),
            recruitment.getDeadlineDate(),
            dDayOf(recruitment.getStatus(), recruitment.getDeadlineDate(), today),
            recruitment.getStatus(),
            recruitment.getRecruitmentType(),
            recruitment.getCurrentParticipants(),
            recruitment.getMaxParticipants(),
            recruitment.getRoles().stream()
                .map(role -> roleOf(role, recruitmentClosed))
                .toList()
        );
    }

    /**
     * 모집글이 마감되었거나 지원 마감일이 지나면 정원이 남은 역할도 마감으로 본다.
     */
    private static boolean isRecruitmentClosed(TeamRecruitment recruitment, LocalDate today) {
        if (!recruitment.isRecruiting()) {
            return true;
        }
        LocalDate deadline = recruitment.getDeadlineDate();
        return deadline != null && today != null && today.isAfter(deadline);
    }

    /**
     * 모집 상태가 RECRUITING이고 지원 마감일이 지나지 않았을 때만 D-day를 반환한다.
     */
    public static Integer dDayOf(
        TeamRecruitmentStatus status,
        LocalDate deadlineDate,
        LocalDate today
    ) {
        if (status != TeamRecruitmentStatus.RECRUITING
            || deadlineDate == null
            || today == null
            || today.isAfter(deadlineDate)) {
            return null;
        }
        return (int) ChronoUnit.DAYS.between(today, deadlineDate);
    }

    public static RecruitmentRole roleOf(TeamRecruitmentRole role, boolean recruitmentClosed) {
        return new RecruitmentRole(
            role.getId(),
            role.getName(),
            role.getCurrentParticipants(),
            role.getMaxParticipants(),
            recruitmentClosed || role.isClosed()
        );
    }
}
