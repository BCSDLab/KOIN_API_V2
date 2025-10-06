package in.koreatech.koin.admin.benefit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.benefit.exception.BenefitNotFoundException;
import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface AdminBenefitCategoryRepository extends Repository<BenefitCategory, Integer> {

    BenefitCategory save(BenefitCategory benefitCategory);

    Optional<BenefitCategory> findById(Integer benefitId);

    List<BenefitCategory> findAllByOrderByTitleAsc();

    void deleteById(Integer id);

    int count();

    boolean existsByTitle(String title);

    boolean existsByTitleAndIdNot(String title, Integer categoryId);

    default BenefitCategory getById(Integer benefitId) {
        return findById(benefitId)
            .orElseThrow(() -> BenefitNotFoundException.withDetail("해당 ID의 혜택 카테고리가 존재하지 않습니다."));
    }
}
