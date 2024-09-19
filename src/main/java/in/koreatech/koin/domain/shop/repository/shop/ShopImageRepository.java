package in.koreatech.koin.domain.shop.repository.shop;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.shop.ShopImage;

public interface ShopImageRepository extends Repository<ShopImage, Integer> {

    ShopImage save(ShopImage shopImage);

    List<ShopImage> findAllByShopId(Integer shopId);
}
