package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import in.koreatech.koin.domain.benefit.model.BenefitCategoryMap;
import in.koreatech.koin.domain.benefit.repository.BenefitCategoryMapRepository;
import in.koreatech.koin.domain.benefit.repository.BenefitCategoryRepository;
import in.koreatech.koin.domain.shop.model.shop.Shop;

@Component
public class BenefitCategoryMapFixture {

    private final BenefitCategoryMapRepository benefitCategoryMapRepository;

    public BenefitCategoryMapFixture(
        BenefitCategoryMapRepository benefitCategoryMapRepository
    ) {
        this.benefitCategoryMapRepository = benefitCategoryMapRepository;
    }

    public BenefitCategoryMap 혜택_추가(Shop shop, BenefitCategory benefitCategory) {
        return benefitCategoryMapRepository.save(BenefitCategoryMap.builder()
            .shop(shop)
            .benefitCategory(benefitCategory)
            .build());
    }

    public BenefitCategoryMap 설명이_포함된_혜택_추가(Shop shop, BenefitCategory benefitCategory, String detail) {
        return benefitCategoryMapRepository.save(BenefitCategoryMap.builder()
                .shop(shop)
                .benefitCategory(benefitCategory)
                .detail(detail)
                .build());
    }
}
