package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.club.repository.AdminClubCategoryRepository;
import in.koreatech.koin.domain.club.model.ClubCategory;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class ClubCategoryFixture {

    private final AdminClubCategoryRepository adminClubCategoryRepository;

    public ClubCategoryFixture(AdminClubCategoryRepository adminClubCategoryRepository) {
        this.adminClubCategoryRepository = adminClubCategoryRepository;
    }

    public ClubCategory 학술() {
        return adminClubCategoryRepository.save(ClubCategory.builder()
            .name("학술")
            .build()
        );
    }

    public ClubCategory 운동() {
        return adminClubCategoryRepository.save(ClubCategory.builder()
            .name("운동")
            .build()
        );
    }

    public ClubCategory 취미() {
        return adminClubCategoryRepository.save(ClubCategory.builder()
            .name("취미")
            .build()
        );
    }

    public ClubCategory 종교() {
        return adminClubCategoryRepository.save(ClubCategory.builder()
            .name("종교")
            .build()
        );
    }

    public ClubCategory 공연() {
        return adminClubCategoryRepository.save(ClubCategory.builder()
            .name("공연")
            .build()
        );
    }
}
