package in.koreatech.koin.admin.benefit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.benefit.model.BenefitCategoryMap;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface AdminBenefitCategoryMapRepository extends CrudRepository<BenefitCategoryMap, Integer> {

    List<BenefitCategoryMap> findAllByIdIn(List<Integer> ids);

    @Query("""
        SELECT bcm 
        FROM BenefitCategoryMap bcm
        JOIN FETCH bcm.shop
        WHERE bcm.benefitCategory.id = :benefitId
        ORDER BY bcm.shop.name ASC
        """)
    List<BenefitCategoryMap> findAllByBenefitCategoryIdOrderByShopName(@Param("benefitId") Integer benefitId);

    @Query("""
        SELECT bcm
        FROM BenefitCategoryMap bcm
        WHERE bcm.benefitCategory.id = :benefitId AND bcm.shop.id IN :shopIds
    """)
    List<BenefitCategoryMap> findAllByBenefitCategoryIdAndShopIds(
        @Param("benefitId") Integer benefitId,
        @Param("shopIds") List<Integer> shopIds
    );

    @Modifying
    @Query("""
        DELETE FROM BenefitCategoryMap bcm 
        WHERE bcm.benefitCategory.id = :benefitId 
        AND bcm.shop.id IN :shopIds
        """)
    void deleteByBenefitCategoryIdAndShopIds(
            @Param("benefitId") Integer benefitId,
            @Param("shopIds") List<Integer> shopIds);

    @Modifying
    @Query("""
        DELETE FROM BenefitCategoryMap bcm 
        WHERE bcm.benefitCategory.id = :benefitId
        """)
    void deleteByBenefitCategoryId(@Param("benefitId") Integer benefitId);
}
