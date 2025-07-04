package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.banner.repository.AdminBannerRepository;
import in.koreatech.koin.domain.banner.model.Banner;
import in.koreatech.koin.domain.banner.model.BannerCategory;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class BannerAcceptanceFixture {

    private final AdminBannerRepository bannerRepository;

    public BannerAcceptanceFixture(AdminBannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    public Banner 메인_배너_1(BannerCategory bannerCategory) {
        return bannerRepository.save(Banner.builder()
            .bannerCategory(bannerCategory)
                .priority(1)
                .title("천원의 아침식사")
                .imageUrl("https://example.com/1000won.jpg")
                .isWebReleased(true)
                .webRedirectLink("https://example.com/1000won")
                .isAndroidReleased(true)
                .androidRedirectLink("koin://1000won")
                .androidMinimumVersion("3.0.14")
                .isIosReleased(true)
                .iosRedirectLink("https://example.com/1000won")
                .iosMinimumVersion("3.0.14")
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
            .isWebReleased(true)
            .webRedirectLink("https://example.com/koin-event")
            .isAndroidReleased(true)
            .androidRedirectLink("koin://koin-event")
            .androidMinimumVersion("3.0.14")
            .isIosReleased(true)
            .iosRedirectLink("https://example.com/koin-event")
            .iosMinimumVersion("3.0.14")
            .isActive(true)
            .build()
        );
    }

    public Banner 메인_배너_3(BannerCategory bannerCategory) {
        return bannerRepository.save(Banner.builder()
            .bannerCategory(bannerCategory)
            .priority(null)
            .title("코인 이벤트 누누")
            .imageUrl("https://example.com/nunu-event.jpg")
            .isWebReleased(true)
            .webRedirectLink("https://example.com/nunu-event")
            .isAndroidReleased(true)
            .androidRedirectLink("koin://nunu-event")
            .androidMinimumVersion("3.0.14")
            .isIosReleased(true)
            .iosRedirectLink("https://example.com/nunu-event")
            .iosMinimumVersion("3.0.14")
            .isActive(false)
            .build()
        );
    }
}
