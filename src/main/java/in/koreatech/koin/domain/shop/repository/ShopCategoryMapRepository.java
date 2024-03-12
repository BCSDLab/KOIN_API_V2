package in.koreatech.koin.domain.shop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.shop.exception.ShopCategoryMapNotFoundException;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.model.ShopCategoryMap;

public interface ShopCategoryMapRepository extends Repository<ShopCategoryMap, Long> {

    ShopCategoryMap save(ShopCategoryMap shopCategoryMap);

}
