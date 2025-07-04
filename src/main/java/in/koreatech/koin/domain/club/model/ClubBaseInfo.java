package in.koreatech.koin.domain.club.model;

import static in.koreatech.koin.domain.club.enums.ClubRecruitmentStatus.*;

import in.koreatech.koin.domain.club.enums.ClubRecruitmentStatus;

public record ClubBaseInfo(
    Integer clubId,
    String name,
    String category,
    Integer likes,
    String imageUrl,
    Boolean isLiked,
    Boolean isLikeHidden,
    Integer recruitmentPeriod,
    Boolean isAlwaysRecruiting
) {
    public ClubRecruitmentStatus getRecruitmentStatus() {
        if (isAlwaysRecruiting == null || recruitmentPeriod == null) {
            return NONE;
        }

        if (isAlwaysRecruiting) {
            return ALWAYS;
        }

        return recruitmentPeriod >= 0
            ? RECRUITING
            : CLOSED;
    }

    public Integer getRecruitmentPeriod() {
        if (isAlwaysRecruiting == null || recruitmentPeriod == null || isAlwaysRecruiting) {
            return null;
        }

        return recruitmentPeriod >= 0 ? recruitmentPeriod : null;
    }
}
