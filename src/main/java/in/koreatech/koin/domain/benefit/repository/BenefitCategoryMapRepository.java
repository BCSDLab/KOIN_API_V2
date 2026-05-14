package in.koreatech.koin.domain.benefit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.benefit.model.BenefitCategoryMap;

public interface BenefitCategoryMapRepository extends Repository<BenefitCategoryMap, Integer> {

    @Query("""
            SELECT bcm FROM BenefitCategoryMap bcm
            JOIN FETCH bcm.shop s
            WHERE bcm.benefitCategory.id = :benefitCategoryId
            AND s.isDeleted = false
            """)
    List<BenefitCategoryMap> findActiveShopMapsByBenefitCategoryId(@Param("benefitCategoryId") Integer benefitCategoryId);

    @Query("""
            SELECT bcm FROM BenefitCategoryMap bcm
            JOIN FETCH bcm.shop s
            JOIN FETCH bcm.benefitCategory bc
            """)
    List<BenefitCategoryMap> findAllWithFetchJoin();

    BenefitCategoryMap save(BenefitCategoryMap benefitCategoryMap);
}
