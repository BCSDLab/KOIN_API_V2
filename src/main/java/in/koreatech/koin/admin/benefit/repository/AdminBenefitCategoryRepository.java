package in.koreatech.koin.admin.benefit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.benefit.exception.BenefitNotFoundException;
import in.koreatech.koin.domain.benefit.model.BenefitCategory;

public interface AdminBenefitCategoryRepository extends Repository<BenefitCategory, Integer> {

    BenefitCategory save(BenefitCategory benefitCategory);

    Optional<BenefitCategory> findById(Integer benefitId);

    Optional<BenefitCategory> findByTitle(String title);

    List<BenefitCategory> findAllByOrderByTitleAsc();

    void deleteById(Integer id);

    int count();

    default BenefitCategory getById(Integer benefitId) {
        return findById(benefitId)
            .orElseThrow(() -> BenefitNotFoundException.withDetail("해당 ID의 혜택 카테고리가 존재하지 않습니다."));
    }
}
