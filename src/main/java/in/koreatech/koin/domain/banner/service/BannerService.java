package in.koreatech.koin.domain.banner.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.banner.dto.response.BannersResponse;
import in.koreatech.koin.domain.banner.enums.PlatformType;
import in.koreatech.koin.domain.banner.model.Banner;
import in.koreatech.koin.domain.banner.repository.BannerRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerRepository bannerRepository;

    public BannersResponse getBannerByCategoryAndPlatform(Integer categoryId, PlatformType platform) {
        List<Banner> banners = bannerRepository.findAllByBannerCategoryIdAndIsActiveTrueOrderByPriority(categoryId);
        return BannersResponse.of(banners, platform);
    }
}
