package in.koreatech.koin.domain.club.recruitment.enums;

import java.time.LocalDate;

import lombok.Getter;

@Getter
public enum ClubRecruitmentStatus {
    NONE,
    BEFORE,
    RECRUITING,
    CLOSED,
    ALWAYS,
    ;

    public static ClubRecruitmentStatus from(
        Boolean isAlwaysRecruiting,
        LocalDate startDate,
        LocalDate endDate
    ) {
        LocalDate today = LocalDate.now();

        if (isAlwaysRecruiting == null && startDate == null && endDate == null) {
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
}
