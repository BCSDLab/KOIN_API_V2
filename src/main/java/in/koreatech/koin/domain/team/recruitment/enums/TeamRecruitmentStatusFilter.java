package in.koreatech.koin.domain.team.recruitment.enums;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TeamRecruitmentStatusFilter {

    ALL(List.of(TeamRecruitmentStatus.RECRUITING, TeamRecruitmentStatus.CLOSED)),
    RECRUITING(List.of(TeamRecruitmentStatus.RECRUITING)),
    CLOSED(List.of(TeamRecruitmentStatus.CLOSED));

    /**
     * 삭제된 모집글은 어떤 필터에서도 노출되지 않는다.
     */
    private final List<TeamRecruitmentStatus> statuses;
}
