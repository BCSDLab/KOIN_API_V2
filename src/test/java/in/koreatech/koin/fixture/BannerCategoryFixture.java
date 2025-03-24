package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.banner.repository.AdminBannerCategoryRepository;
import in.koreatech.koin.domain.banner.model.BannerCategory;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class BannerCategoryFixture {

    private final AdminBannerCategoryRepository bannerCategoryRepository;

    public BannerCategoryFixture(AdminBannerCategoryRepository bannerCategoryRepository) {
        this.bannerCategoryRepository = bannerCategoryRepository;
    }

    public BannerCategory 메인_모달() {
        return bannerCategoryRepository.save(BannerCategory.builder()
            .name("메인 모달")
            .build()
        );
    }
}
