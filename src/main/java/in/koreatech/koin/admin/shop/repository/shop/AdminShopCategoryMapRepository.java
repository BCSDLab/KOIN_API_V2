package in.koreatech.koin.admin.shop.repository.shop;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.shop.ShopCategoryMap;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface AdminShopCategoryMapRepository extends Repository<ShopCategoryMap, Integer> {

    ShopCategoryMap save(ShopCategoryMap shopCategoryMap);

    List<ShopCategoryMap> findAllByShopId(Integer shopId);

    boolean existsByShopCategoryId(Integer categoryId);

}
