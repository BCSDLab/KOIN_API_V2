package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.banner.repository.AdminBannerRepository;
import in.koreatech.koin.domain.banner.model.Banner;
import in.koreatech.koin.domain.banner.model.BannerCategory;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class BannerFixture {

    private final AdminBannerRepository bannerRepository;

    public BannerFixture(AdminBannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    public Banner 메인_배너_1(BannerCategory bannerCategory) {
        return bannerRepository.save(Banner.builder()
            .bannerCategory(bannerCategory)
                .priority(1)
                .title("천원의 아침식사")
                .imageUrl("https://example.com/1000won.jpg")
                .webRedirectLink("https://example.com/1000won")
                .androidRedirectLink("https://example.com/1000won")
                .iosRedirectLink("https://example.com/1000won")
                .isActive(true)
            .build()
            );
    }

    public Banner 메인_배너_2(BannerCategory bannerCategory) {
        return bannerRepository.save(Banner.builder()
            .bannerCategory(bannerCategory)
            .priority(2)
            .title("코인 이벤트")
            .imageUrl("https://example.com/koin-event.jpg")
            .webRedirectLink("https://example.com/koin-event")
            .androidRedirectLink("https://example.com/koin-event")
            .iosRedirectLink("https://example.com/koin-event")
            .isActive(true)
            .build()
        );
    }
}
