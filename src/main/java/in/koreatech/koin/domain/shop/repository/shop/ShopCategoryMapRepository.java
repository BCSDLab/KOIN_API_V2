package in.koreatech.koin.domain.shop.repository.shop;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.shop.ShopCategoryMap;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface ShopCategoryMapRepository extends Repository<ShopCategoryMap, Integer> {

    ShopCategoryMap save(ShopCategoryMap shopCategoryMap);

    List<ShopCategoryMap> findAllByShopId(Integer shopId);
}
