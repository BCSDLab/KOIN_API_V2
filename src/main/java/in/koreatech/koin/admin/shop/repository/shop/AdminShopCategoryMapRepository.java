package in.koreatech.koin.admin.shop.repository.shop;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.shop.ShopCategoryMap;

public interface AdminShopCategoryMapRepository extends Repository<ShopCategoryMap, Integer> {

    ShopCategoryMap save(ShopCategoryMap shopCategoryMap);

    List<ShopCategoryMap> findAllByShopId(Integer shopId);

    boolean existsByShopCategoryId(Integer categoryId);

}
