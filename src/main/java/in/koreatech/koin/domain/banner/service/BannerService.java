package in.koreatech.koin.domain.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;
import in.koreatech.koin.domain.banner.dto.ChangeBannerActiveRequest;
import in.koreatech.koin.domain.banner.dto.ChangeBannerPriorityRequest;
import in.koreatech.koin.domain.banner.enums.PriorityChangeType;
import in.koreatech.koin.domain.banner.model.Banner;
import in.koreatech.koin.domain.banner.model.BannerCategory;
import in.koreatech.koin.domain.banner.repository.BannerRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerRepository bannerRepository;

    @Transactional
    public void changePriority(Integer bannerId, ChangeBannerPriorityRequest request) {
        PriorityChangeType changeType = request.changeType();
        Banner banner = bannerRepository.getById(bannerId);
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
        Banner upper = bannerRepository
            .findByBannerCategoryAndPriorityAndIsActiveTrue(category, currentPriority - 1)
            .orElseThrow(() -> new KoinIllegalStateException("우선순위 변경 대상 배너가 존재하지 않습니다. bannerId: " + banner.getId()));
        banner.updatePriority(currentPriority - 1);
        upper.updatePriority(currentPriority);
    }

    private void priorityMoveDown(Integer currentPriority, BannerCategory category, Banner banner) {
        Integer maxPriority = bannerRepository.findMaxPriorityCategory(category);
        if (maxPriority == null || currentPriority >= maxPriority) return;
        Banner lower = bannerRepository
            .findByBannerCategoryAndPriorityAndIsActiveTrue(category, currentPriority + 1)
            .orElseThrow(() -> new KoinIllegalStateException("우선순위 변경 대상 배너가 존재하지 않습니다. bannerId: " + banner.getId()));
        banner.updatePriority(currentPriority + 1);
        lower.updatePriority(currentPriority);
    }

    @Transactional
    public void changeActive(Integer bannerId, ChangeBannerActiveRequest request) {
        Banner banner = bannerRepository.getById(bannerId);
        boolean before = banner.getIsActive();
        boolean after = request.isActive();
        if (before == after) return;

        if (after) {
            Integer maxPriority = bannerRepository.findMaxPriorityCategory(banner.getBannerCategory());
            banner.updatePriority(maxPriority + 1);
        } else {
            banner.updatePriority(null);
        }
    }
}
