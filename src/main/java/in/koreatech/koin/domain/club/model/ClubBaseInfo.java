package in.koreatech.koin.domain.club.model;

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
        return ClubRecruitmentStatus.from(isAlwaysRecruiting, startDate, endDate);
    }

    public Integer getRecruitmentPeriod() {
        ClubRecruitmentStatus clubRecruitmentStatus = ClubRecruitmentStatus.from(isAlwaysRecruiting, startDate, endDate);
        return ClubRecruitmentDday.from(clubRecruitmentStatus, endDate).getDday();
    }
}
