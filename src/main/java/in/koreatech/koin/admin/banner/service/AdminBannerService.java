package in.koreatech.koin.admin.banner.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;
import in.koreatech.koin._common.model.Criteria;
import in.koreatech.koin.admin.banner.dto.request.AdminBannerCreateRequest;
import in.koreatech.koin.admin.banner.dto.request.ChangeBannerActiveRequest;
import in.koreatech.koin.admin.banner.dto.request.ChangeBannerPriorityRequest;
import in.koreatech.koin.admin.banner.dto.response.AdminBannerResponse;
import in.koreatech.koin.admin.banner.dto.response.AdminBannersResponse;
import in.koreatech.koin.admin.banner.enums.PriorityChangeType;
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

    // TODO. 우선순위 삭제 로직 추가
    @Transactional
    public void deleteBanner(Integer bannerId) {
        adminBannerRepository.deleteById(bannerId);
    }

    @Transactional
    public void changePriority(Integer bannerId, ChangeBannerPriorityRequest request) {
        PriorityChangeType changeType = request.changeType();
        Banner banner = adminBannerRepository.getById(bannerId);
        validateBannerIsActive(bannerId, banner);
        Integer currentPriority = banner.getPriority();
        validateBannerPriority(bannerId, currentPriority);
        BannerCategory category = banner.getBannerCategory();
        switch (changeType) {
            case UP -> priorityMoveUp(currentPriority, category, banner);
            case DOWN -> priorityMoveDown(currentPriority, category, banner);
        }
    }

    private void validateBannerIsActive(Integer bannerId, Banner banner) {
        if (!banner.getIsActive()) {
            throw new KoinIllegalStateException("비활성화된 배너에 대한 우선순위 변경 요청입니다. bannerId: " + bannerId);
        }
    }

    private void validateBannerPriority(Integer bannerId, Integer currentPriority) {
        if (currentPriority == null) {
            throw new KoinIllegalStateException("활성화되었지만 우선순위가 설정되지 않은 배너입니다. bannerId: " + bannerId);
        }
    }

    private void priorityMoveUp(Integer currentPriority, BannerCategory category, Banner banner) {
        if (currentPriority <= 0) return;
        Banner upper = adminBannerRepository
            .findByBannerCategoryAndPriorityAndIsActiveTrue(category, currentPriority - 1)
            .orElseThrow(() -> new KoinIllegalStateException("우선순위 변경 대상 배너가 존재하지 않습니다. bannerId: " + banner.getId()));
        banner.updatePriority(currentPriority - 1);
        upper.updatePriority(currentPriority);
    }

    private void priorityMoveDown(Integer currentPriority, BannerCategory category, Banner banner) {
        Integer maxPriority = adminBannerRepository.findMaxPriorityCategory(category);
        if (maxPriority == null || currentPriority >= maxPriority) return;
        Banner lower = adminBannerRepository
            .findByBannerCategoryAndPriorityAndIsActiveTrue(category, currentPriority + 1)
            .orElseThrow(() -> new KoinIllegalStateException("우선순위 변경 대상 배너가 존재하지 않습니다. bannerId: " + banner.getId()));
        banner.updatePriority(currentPriority + 1);
        lower.updatePriority(currentPriority);
    }

    @Transactional
    public void changeActive(Integer bannerId, ChangeBannerActiveRequest request) {
        Banner banner = adminBannerRepository.getById(bannerId);
        boolean before = banner.getIsActive();
        boolean after = request.isActive();
        if (before == after) return;

        if (after) {
            Integer maxPriority = adminBannerRepository.findMaxPriorityCategory(banner.getBannerCategory());
            banner.updatePriority(maxPriority + 1);
        } else {
            banner.updatePriority(null);
        }
    }
}
