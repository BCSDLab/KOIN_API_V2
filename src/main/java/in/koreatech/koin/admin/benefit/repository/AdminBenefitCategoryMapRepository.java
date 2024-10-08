package in.koreatech.koin.admin.benefit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.benefit.model.BenefitCategoryMap;

public interface AdminBenefitCategoryMapRepository extends Repository<BenefitCategoryMap, Integer> {

    void save(BenefitCategoryMap benefitCategoryMap);

    @Query("""
        SELECT bcm 
        FROM BenefitCategoryMap bcm
        JOIN FETCH bcm.shop
        WHERE bcm.benefitCategory.id = :benefitId
        ORDER BY bcm.shop.name ASC
        """)
    List<BenefitCategoryMap> findAllByBenefitCategoryIdOrderByShopName(Integer benefitId);

    @Modifying
    @Query("""
        DELETE FROM BenefitCategoryMap bcm 
        WHERE bcm.benefitCategory.id = :benefitId 
        AND bcm.shop.id IN :shopIds
        """)
    void deleteByBenefitCategoryIdAndShopIds(Integer benefitId, List<Integer> shopIds);

    @Modifying
    @Query("""
        DELETE FROM BenefitCategoryMap bcm 
        WHERE bcm.benefitCategory.id = :benefitId
        """)
    void deleteByBenefitCategoryId(Integer benefitId);

}
