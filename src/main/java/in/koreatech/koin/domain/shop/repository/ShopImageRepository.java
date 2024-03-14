package in.koreatech.koin.domain.shop.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.shop.exception.ShopImageNotFoundException;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.model.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.ShopImage;
import in.koreatech.koin.domain.shop.model.ShopOpen;

public interface ShopImageRepository extends Repository<ShopImage, Long> {

    ShopImage save(ShopImage shopImage);

    List<ShopImage> findAllByShopId(Long shopId);

    default List<ShopImage> saveAll(List<ShopImage> shopImages) {
        List<ShopImage> result = new ArrayList<>(shopImages.size());
        shopImages.forEach(shopImage -> {
            result.add(save(shopImage));
        });
        return result;
    }
}
