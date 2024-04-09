package in.koreatech.koin.domain.shop.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.ShopImage;

public interface ShopImageRepository extends Repository<ShopImage, Long> {

    ShopImage save(ShopImage shopImage);

    List<ShopImage> findAllByShopId(Integer shopId);
}
