package in.koreatech.koin.unit.fixture;

import java.time.LocalDateTime;

import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubCategory;

public class ClubFixture {

    private ClubFixture() {}

    public static Club 활성화_BCSD_동아리(Integer id) {
        ClubCategory category = ClubCategory.builder()
            .id(1)
            .name("학술")
            .build();

        Club club = Club.builder()
            .id(id)
            .name("BCSD Lab")
            .normalizedName("BCSDLab")
            .hits(1234)
            .lastWeekHits(1000)
            .description("즐겁게 일하고 열심히 노는 IT 특성화 동아리")
            .isActive(true)
            .imageUrl("https://bcsdlab.com/static/img/logo.d89d9cc.png")
            .likes(100)
            .location("학생회관")
            .introduction("안녕하세요 BCSD Lab입니다")
            .isLikeHidden(false)
            .clubCategory(category)
            .build();

        ReflectionTestUtils.setField(club, "updatedAt", LocalDateTime.now());

        return club;
    }

    public static Club 비활성화_BCSD_동아리(Integer id) {
        ClubCategory category = ClubCategory.builder()
            .id(1)
            .name("학술")
            .build();

        Club club = Club.builder()
            .id(id)
            .name("BCSD Lab")
            .normalizedName("BCSDLab")
            .hits(1234)
            .lastWeekHits(1000)
            .description("즐겁게 일하고 열심히 노는 IT 특성화 동아리")
            .isActive(false)
            .imageUrl("https://bcsdlab.com/static/img/logo.d89d9cc.png")
            .likes(100)
            .location("학생회관")
            .introduction("안녕하세요 BCSD Lab입니다")
            .isLikeHidden(false)
            .clubCategory(category)
            .build();

        ReflectionTestUtils.setField(club, "updatedAt", LocalDateTime.now());

        return club;
    }


}
