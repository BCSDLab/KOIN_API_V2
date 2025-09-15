package in.koreatech.koin.domain.club.recruitment.model;

import in.koreatech.koin.domain.club.recruitment.enums.ClubRecruitmentStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static in.koreatech.koin.domain.club.recruitment.enums.ClubRecruitmentStatus.RECRUITING;

@Getter
public class ClubRecruitmentDday {

    private Integer dday;

    private ClubRecruitmentDday(Integer dday) {
        this.dday = dday;
    }

    public static ClubRecruitmentDday from(ClubRecruitmentStatus status, LocalDate endDate) {
        if (status == RECRUITING) {
            int days = (int)ChronoUnit.DAYS.between(LocalDate.now(), endDate);
            return new ClubRecruitmentDday(days);
        }
        return new ClubRecruitmentDday(null);
    }
}
