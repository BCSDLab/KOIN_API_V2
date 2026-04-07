package in.koreatech.koin.admin.benefit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.benefit.model.BenefitCategoryMap;

public interface AdminBenefitCategoryMapRepository extends Repository<BenefitCategoryMap, Integer> {

    <S extends BenefitCategoryMap> List<S> saveAll(Iterable<S> entities);

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
