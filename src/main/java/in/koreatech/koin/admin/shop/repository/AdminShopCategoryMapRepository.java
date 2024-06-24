package in.koreatech.koin.admin.shop.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.ShopCategoryMap;

public interface AdminShopCategoryMapRepository extends Repository<ShopCategoryMap, Integer> {

    ShopCategoryMap save(ShopCategoryMap shopCategoryMap);
}
