package in.koreatech.koin.admin.shop.repository.shop;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.shop.exception.ShopParentCategoryNotFoundException;
import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface AdminShopParentCategoryRepository extends Repository<ShopParentCategory, Integer> {

    ShopParentCategory save(ShopParentCategory shopParentCategory);

    Optional<ShopParentCategory> findById(Integer shopParentCategoryId);

    List<ShopParentCategory> findAll();

    default ShopParentCategory getById(Integer shopParentCategoryId) {
        return findById(shopParentCategoryId)
            .orElseThrow(
                () -> ShopParentCategoryNotFoundException.withDetail("shopParentCategoryId: " + shopParentCategoryId)
            );
    }
}
