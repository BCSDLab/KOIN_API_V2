package in.koreatech.koin.domain.shop.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.shop.exception.ShopCategoryMapNotFoundException;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.model.ShopCategoryMap;

public interface ShopCategoryMapRepository extends Repository<ShopCategoryMap, Long> {

    ShopCategoryMap save(ShopCategoryMap shopCategoryMap);

    List<ShopCategoryMap> findAllByShopId(Long shopId);

    default List<ShopCategoryMap> saveAll(List<ShopCategoryMap> shopCategoryMaps) {
        List<ShopCategoryMap> result = new ArrayList<>(shopCategoryMaps.size());
        shopCategoryMaps.forEach(shopCategoryMap -> {
            result.add(save(shopCategoryMap));
        });
        return result;
    }

}
