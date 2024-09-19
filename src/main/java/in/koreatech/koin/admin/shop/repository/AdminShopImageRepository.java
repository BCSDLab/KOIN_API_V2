package in.koreatech.koin.admin.shop.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.shop.ShopImage;

public interface AdminShopImageRepository extends Repository<ShopImage, Integer> {

    ShopImage save(ShopImage shopImage);

    List<ShopImage> findAllByShopId(Integer shopId);
}
