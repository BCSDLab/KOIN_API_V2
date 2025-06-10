package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.banner.repository.AdminBannerCategoryRepository;
import in.koreatech.koin.domain.banner.model.BannerCategory;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class BannerCategoryAcceptanceFixture {

    private final AdminBannerCategoryRepository bannerCategoryRepository;

    public BannerCategoryAcceptanceFixture(AdminBannerCategoryRepository bannerCategoryRepository) {
        this.bannerCategoryRepository = bannerCategoryRepository;
    }

    public BannerCategory 메인_모달() {
        return bannerCategoryRepository.save(BannerCategory.builder()
            .name("메인 모달")
            .description("140*112 앱/웹 랜딩시 뜨는 모달입니다.")
            .build()
        );
    }
}
