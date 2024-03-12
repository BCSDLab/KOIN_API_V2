package in.koreatech.koin.domain.shop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.shop.exception.ShopCategoryMapNotFoundException;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.model.ShopCategoryMap;

public interface ShopCategoryMapRepository extends Repository<ShopCategoryMap, Long> {

    Optional<ShopCategoryMap> findById(Long shopCategroyMapId);

    ShopCategoryMap save(Menu menu);

    default ShopCategoryMap getById(Long shopCategroyMapId) {
        return findById(shopCategroyMapId).orElseThrow(() -> ShopCategoryMapNotFoundException.withDetail("shopCategroyMapId: " + shopCategroyMapId));
    }
}
