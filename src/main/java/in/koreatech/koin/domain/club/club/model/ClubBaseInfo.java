package in.koreatech.koin.domain.club.club.model;

import java.time.LocalDate;

import in.koreatech.koin.domain.club.recruitment.enums.ClubRecruitmentStatus;
import in.koreatech.koin.domain.club.recruitment.model.ClubRecruitmentDday;

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
