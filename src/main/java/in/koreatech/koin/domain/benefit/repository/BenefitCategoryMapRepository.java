package in.koreatech.koin.domain.benefit.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import in.koreatech.koin.domain.benefit.model.BenefitCategoryMap;

public interface BenefitCategoryMapRepository extends Repository<BenefitCategoryMap, Integer> {

    List<BenefitCategoryMap> findAllByBenefitCategoryId(Integer benefitId);
}
