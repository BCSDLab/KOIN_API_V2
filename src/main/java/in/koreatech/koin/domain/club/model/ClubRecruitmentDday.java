package in.koreatech.koin.domain.club.model;

import static in.koreatech.koin.domain.club.enums.ClubRecruitmentStatus.RECRUITING;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import in.koreatech.koin.domain.club.enums.ClubRecruitmentStatus;
import lombok.Getter;

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
