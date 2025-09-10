package in.koreatech.koin.unit.fixture;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubHot;

import java.time.LocalDate;

public class ClubHotFixture {

    private ClubHotFixture() { }

    public static ClubHot 인기_동아리(Integer id, Club club) {
        ClubHot clubHot = ClubHot.builder()
            .id(id)
            .club(club)
            .ranking(1)
            .periodHits(100)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now())
            .build();

        return clubHot;
    }

}
