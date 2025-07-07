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
        LocalDate today = LocalDate.now();

        if (isUndefined()) {
            return NONE;
        }

        if (Boolean.TRUE.equals(isAlwaysRecruiting)) {
            return ALWAYS;
        }

        if (startDate != null && today.isBefore(startDate)) {
            return BEFORE;
        }

        if (endDate != null && today.isAfter(endDate)) {
            return CLOSED;
        }

        return RECRUITING;
    }

    public Integer getRecruitmentPeriod() {
        LocalDate today = LocalDate.now();

        if (isUndefined() || Boolean.TRUE.equals(isAlwaysRecruiting)) {
            return null;
        }

        if (startDate == null || endDate == null || today.isAfter(endDate)) {
            return null;
        }

        int period = (int) DAYS.between(startDate, endDate);
        return period >= 0 ? period : null;
    }

    private boolean isUndefined() {
        return isAlwaysRecruiting == null && startDate == null && endDate == null;
    }
}
