package in.koreatech.koin.domain.benefit.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.benefit.dto.BenefitCategoryResponse;
import in.koreatech.koin.domain.benefit.dto.BenefitShopsResponse;
import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import in.koreatech.koin.domain.benefit.model.BenefitCategoryMap;
import in.koreatech.koin.domain.benefit.repository.BenefitCategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopBenefitService {

    private final BenefitCategoryRepository benefitCategoryRepository;

    public BenefitCategoryResponse getBenefitCategories() {
        List<BenefitCategory> benefitCategories = benefitCategoryRepository.findAll();
        return BenefitCategoryResponse.from(benefitCategories);
    }

    public BenefitShopsResponse getBenefitShops(Integer benefitId) {
        // List<BenefitCategoryMap> benefitCategories = benefitCategoryMapRepository.findAllByBenefitCategoryId(benefitId);
        return BenefitShopsResponse.from();
    }
}
