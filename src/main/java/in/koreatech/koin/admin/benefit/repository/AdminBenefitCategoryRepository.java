package in.koreatech.koin.admin.benefit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.benefit.model.BenefitCategory;

public interface AdminBenefitCategoryRepository extends Repository<BenefitCategory, Integer> {

    BenefitCategory save(BenefitCategory benefitCategory);

    Optional<BenefitCategory> findById(Integer benefitId);

    List<BenefitCategory> findAllByOrderByTitleAsc();

    void deleteById(Integer categoryId);

    default BenefitCategory getById(Integer benefitId) {
        return findById(benefitId)
            .orElseThrow(() -> new IllegalArgumentException("BenefitCategory not found"));
    }
}
