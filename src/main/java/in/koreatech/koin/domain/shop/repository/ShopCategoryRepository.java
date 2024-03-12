package in.koreatech.koin.domain.shop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.shop.exception.ShopCategoryNotFoundException;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.ShopCategory;

public interface ShopCategoryRepository extends Repository<ShopCategory, Long> {

    Optional<ShopCategory> findById(Long shopCategoryId);

    ShopCategory save(ShopCategory shopCategory);

    default ShopCategory getById(Long shopCategoryId) {
        return findById(shopCategoryId).orElseThrow(() -> ShopCategoryNotFoundException.withDetail("shopCategoryId: " + shopCategoryId));
    }
}
