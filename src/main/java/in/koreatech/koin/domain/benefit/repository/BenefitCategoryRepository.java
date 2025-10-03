package in.koreatech.koin.domain.benefit.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface BenefitCategoryRepository extends Repository<BenefitCategory, Integer> {

    List<BenefitCategory> findAll();

    BenefitCategory save(BenefitCategory benefitCategory);
}
