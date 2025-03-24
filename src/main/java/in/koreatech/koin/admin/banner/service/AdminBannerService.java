package in.koreatech.koin.admin.banner.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.model.Criteria;
import in.koreatech.koin.admin.banner.dto.request.AdminBannerCreateRequest;
import in.koreatech.koin.admin.banner.dto.response.AdminBannerResponse;
import in.koreatech.koin.admin.banner.dto.response.AdminBannersResponse;
import in.koreatech.koin.admin.banner.repository.AdminBannerCategoryRepository;
import in.koreatech.koin.admin.banner.repository.AdminBannerRepository;
import in.koreatech.koin.domain.banner.model.Banner;
import in.koreatech.koin.domain.banner.model.BannerCategory;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminBannerService {

    private final AdminBannerRepository adminBannerRepository;
    private final AdminBannerCategoryRepository adminBannerCategoryRepository;

    public AdminBannerResponse getBanner(Integer bannerId) {
        Banner banner = adminBannerRepository.getById(bannerId);
        return AdminBannerResponse.from(banner);
    }

    public AdminBannersResponse getBanners(Integer page, Integer limit, Boolean isActive, String bannerCategoryName) {
        BannerCategory bannerCategory = adminBannerCategoryRepository.getByName(bannerCategoryName);
        Integer bannerCategoryId = bannerCategory.getId();

        boolean hasIsActive = isActive != null;

        int total = hasIsActive
            ? adminBannerRepository.countByIsActiveAndBannerCategoryId(isActive, bannerCategoryId)
            : adminBannerRepository.countByBannerCategoryId(bannerCategoryId);

        Criteria criteria = Criteria.of(page, limit, total);

        Sort sort = Sort.by(Sort.Direction.ASC, hasIsActive ? "priority" : "created_at");
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), sort);

        Page<Banner> banners = hasIsActive
            ? adminBannerRepository.findAllByIsActiveAndBannerCategoryId(isActive, bannerCategoryId, pageRequest)
            : adminBannerRepository.findAllByBannerCategoryId(bannerCategoryId, pageRequest);

        return AdminBannersResponse.from(banners);
    }

    @Transactional
    public void createBanner(AdminBannerCreateRequest request) {
        BannerCategory bannerCategory = adminBannerCategoryRepository.getById(request.bannerCategoryId());
        Banner banner = request.of(bannerCategory);
        adminBannerRepository.save(banner);
    }

    @Transactional
    public void deleteBanner(Integer bannerId) {
        Banner banner = adminBannerRepository.getById(bannerId);
        BannerCategory bannerCategory = banner.getBannerCategory();

        if (banner.getIsActive() && banner.getPriority() != null) {
            adminBannerRepository.findLowerPriorityBannersInCategory(true, bannerCategory.getId(), banner.getPriority())
                .forEach(lowerPriorityBanner -> {
                    Integer priority = lowerPriorityBanner.getPriority();
                    lowerPriorityBanner.updatePriority(priority - 1);
                });
        }
        adminBannerRepository.deleteById(bannerId);
    }
}
