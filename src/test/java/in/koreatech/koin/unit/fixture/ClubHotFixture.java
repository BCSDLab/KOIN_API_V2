package in.koreatech.koin.unit.fixture;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubHot;
import in.koreatech.koin.domain.club.model.redis.ClubHotRedis;

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

    public static ClubHotRedis 인기_동아리_레디스(Integer id, Integer clubId) {
        Club club = ClubFixture.활성화_BCSD_동아리(clubId);

        return ClubHotRedis.from(club);
    }
}
