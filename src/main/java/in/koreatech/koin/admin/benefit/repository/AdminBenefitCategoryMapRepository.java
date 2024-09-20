package in.koreatech.koin.admin.benefit.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import in.koreatech.koin.domain.benefit.model.BenefitCategoryMap;

public interface AdminBenefitCategoryMapRepository extends Repository<BenefitCategoryMap, Integer> {

    void save(BenefitCategoryMap benefitCategoryMap);

    @Query("SELECT bcm FROM BenefitCategoryMap bcm WHERE bcm.benefitCategory.id = :benefitId")
    List<BenefitCategoryMap> findAllByBenefitCategoryId(Integer benefitId);

    @Modifying
    @Query("DELETE FROM BenefitCategoryMap bcm WHERE bcm.benefitCategory.id = :benefitId AND bcm.shop.id IN :shopIds")
    void deleteByBenefitCategoryIdAndShopIds(Integer benefitId, List<Integer> shopIds);

    @Modifying
    @Query("DELETE FROM BenefitCategoryMap bcm WHERE bcm.benefitCategory.id = :categoryId")
    void deleteByBenefitCategoryId(Integer categoryId);
}
