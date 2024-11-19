package in.koreatech.koin.admin.shop.repository.shop;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.shop.exception.ShopParentCategoryNotFoundException;
import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;

public interface AdminShopParentCategoryRepository extends Repository<ShopParentCategory, Integer> {

    ShopParentCategory save(ShopParentCategory shopParentCategory);

    Optional<ShopParentCategory> findById(Integer shopParentCategoryId);

    default ShopParentCategory getById(Integer shopParentCategoryId) {
        return findById(shopParentCategoryId)
            .orElseThrow(() -> ShopParentCategoryNotFoundException.withDetail("shopParentCategoryId: " + shopParentCategoryId));
    }

    List<ShopParentCategory> findAll();
}
