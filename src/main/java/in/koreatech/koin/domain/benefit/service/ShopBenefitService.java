package in.koreatech.koin.domain.benefit.service;

import static in.koreatech.koin.domain.benefit.dto.BenefitShopsResponse.InnerShopResponse;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.benefit.dto.BenefitCategoryResponse;
import in.koreatech.koin.domain.benefit.dto.BenefitShopsResponse;
import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import in.koreatech.koin.domain.benefit.model.BenefitCategoryMap;
import in.koreatech.koin.domain.benefit.repository.BenefitCategoryMapRepository;
import in.koreatech.koin.domain.benefit.repository.BenefitCategoryRepository;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.event.EventArticleRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopBenefitService {

    private final BenefitCategoryRepository benefitCategoryRepository;
    private final BenefitCategoryMapRepository benefitCategoryMapRepository;
    private final EventArticleRepository eventArticleRepository;
    private final Clock clock;

    public BenefitCategoryResponse getBenefitCategories() {
        List<BenefitCategory> benefitCategories = benefitCategoryRepository.findAll();
        return BenefitCategoryResponse.from(benefitCategories);
    }

    public BenefitShopsResponse getBenefitShops(Integer benefitId) {
        List<BenefitCategoryMap> benefitCategoryMaps = benefitCategoryMapRepository.findByBenefitCategoryId(benefitId);
        LocalDateTime now = LocalDateTime.now(clock);

        List<InnerShopResponse> innerShopResponses = benefitCategoryMaps.stream()
            .map(benefitCategoryMap -> {
                Shop shop = benefitCategoryMap.getShop();
                String benefitDetail = benefitCategoryMap.getDetail();
                boolean isDurationEvent = eventArticleRepository.isDurationEvent(shop.getId(), now.toLocalDate());
                return InnerShopResponse.from(
                    shop,
                    isDurationEvent,
                    shop.isOpen(now),
                    benefitDetail
                );
            })
            .sorted(InnerShopResponse.getComparator())
            .toList();

        return BenefitShopsResponse.from(innerShopResponses);
    }
}
