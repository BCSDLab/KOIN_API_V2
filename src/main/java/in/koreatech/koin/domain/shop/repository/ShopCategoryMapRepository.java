package in.koreatech.koin.domain.shop.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.ShopCategoryMap;

public interface ShopCategoryMapRepository extends Repository<ShopCategoryMap, Long> {

    ShopCategoryMap save(ShopCategoryMap shopCategoryMap);

    List<ShopCategoryMap> findAllByShopId(Long shopId);
}
