package in.koreatech.koin.domain.club.model;

import static in.koreatech.koin.domain.club.enums.ClubRecruitmentStatus.*;
import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;

import in.koreatech.koin.domain.club.enums.ClubRecruitmentStatus;

public record ClubBaseInfo(
    Integer clubId,
    String name,
    String category,
    Integer likes,
    String imageUrl,
    Boolean isLiked,
    Boolean isLikeHidden,
    LocalDate startDate,
    LocalDate endDate,
    Boolean isAlwaysRecruiting
) {
    public ClubRecruitmentStatus getRecruitmentStatus() {
        if (isAlwaysRecruiting == null || startDate == null || endDate == null) {
            return NONE;
        }

        if (Boolean.TRUE.equals(isAlwaysRecruiting)) {
            return ALWAYS;
        }

        if (endDate.isBefore(LocalDate.now())) {
            return CLOSED;
        }

        return RECRUITING;
    }

    public Integer getRecruitmentPeriod() {
        if (Boolean.TRUE.equals(isAlwaysRecruiting) || startDate == null || endDate == null) {
            return null;
        }

        if (endDate.isBefore(LocalDate.now())) {
            return null;
        }

        int period = (int) DAYS.between(startDate, endDate);
        return period >= 0 ? period : null;
    }
}
