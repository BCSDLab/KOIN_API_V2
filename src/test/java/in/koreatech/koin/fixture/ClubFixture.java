package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.club.repository.AdminClubRepository;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubCategory;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class ClubFixture {

    private final AdminClubRepository adminClubRepository;

    public ClubFixture(AdminClubRepository adminClubRepository) {
        this.adminClubRepository = adminClubRepository;
    }

    public Club BCSD_동아리(ClubCategory clubCategory) {
        return adminClubRepository.save(Club.builder()
                .name("BCSD")
                .imageUrl("https://bcsdlab.com/static/img/logo.d89d9cc.png")
                .description("즐겁게 일하고 열심히 노는 IT 특성화 동아리")
                .clubCategory(clubCategory)
                .location("학생회관")
            .build()
        );
    }

    public Club 씨앗_동아리(ClubCategory clubCategory) {
        return adminClubRepository.save(Club.builder()
            .name("씨앗")
            .imageUrl("https://www.acmicpc.net/group/5170")
            .description("한국기술교육대학교 학술 소모임 씨앗 그룹입니다.")
            .clubCategory(clubCategory)
            .location("백준")
            .build()
        );
    }
}
