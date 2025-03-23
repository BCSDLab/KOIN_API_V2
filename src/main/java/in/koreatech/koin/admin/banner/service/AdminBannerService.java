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
        Integer total = adminBannerRepository.countByIsActiveAndBannerCategoryId(isActive, bannerCategory.getId());

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest;

        // 활성화 여부에 따른 정렬 조건
        if (isActive) {
            pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
                Sort.by(Sort.Direction.ASC, "priority")
            );
        } else {
            pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
                Sort.by(Sort.Direction.ASC, "createAt")
            );
        }

        Page<Banner> banners = adminBannerRepository.findAllByIsActiveAndBannerCategoryId(isActive,
            bannerCategory.getId(), pageRequest);

        return AdminBannersResponse.from(banners);
    }

    @Transactional
    public void createBanner(AdminBannerCreateRequest request) {
        BannerCategory bannerCategory = adminBannerCategoryRepository.getByName(request.bannerCategory());
        Banner banner = request.of(bannerCategory);
        adminBannerRepository.save(banner);
    }

    public void deleteBanner(Integer bannerId) {
        adminBannerRepository.deleteById(bannerId);
    }
}
