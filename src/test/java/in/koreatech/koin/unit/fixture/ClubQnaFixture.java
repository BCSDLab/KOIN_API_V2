package in.koreatech.koin.unit.fixture;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubQna;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

public class ClubQnaFixture {

    public static ClubQna QNA(Integer clubId, Integer qnaId, String content) {
        Club club = ClubFixture.활성화_BCSD_동아리(clubId);

        ClubQna clubQna = ClubQna.builder()
            .id(qnaId)
            .club(club)
            .content(content)
            .isManager(false)
            .isDeleted(false)
            .build();

        ReflectionTestUtils.setField(clubQna, "createdAt", LocalDateTime.now());

        return clubQna;
    }
}
