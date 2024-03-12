package in.koreatech.koin.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.shop.exception.ShopImageNotFoundException;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.model.ShopImage;

public interface ShopImageRepository extends Repository<ShopImage, Long> {

    Optional<ShopImage> findById(Long shopImageId);

    ShopImage save(ShopImage shopImage);

    default ShopImage getById(Long shopImageId) {
        return findById(shopImageId).orElseThrow(() -> ShopImageNotFoundException.withDetail("shopImageId: " + shopImageId));
    }
}
