package in.koreatech.koin.unit.fixture;

import java.time.LocalDate;

import in.koreatech.koin.domain.club.recruitment.enums.ClubRecruitmentStatus;
import in.koreatech.koin.domain.club.club.model.ClubBaseInfo;

public class ClubBaseInfoFixture {

    private ClubBaseInfoFixture() { }

    public static ClubBaseInfo 동아리_기본_정보(
        Integer id,
        String name,
        String category,
        ClubRecruitmentStatus status
    ) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        Boolean isAlwaysRecruiting = null;

        switch (status) {
            case NONE -> {}
            case RECRUITING -> {
                startDate = LocalDate.now().minusDays(1);
                endDate = LocalDate.now();
                isAlwaysRecruiting = false;
            }
            case ALWAYS -> isAlwaysRecruiting = true;
        }

        return new ClubBaseInfo(
            id,
            name,
            category,
            100,
            "https://bcsdlab.com/static/img/logo.d89d9cc.png",
            true,
            false,
            startDate,
            endDate,
            isAlwaysRecruiting
        );
    }
}
