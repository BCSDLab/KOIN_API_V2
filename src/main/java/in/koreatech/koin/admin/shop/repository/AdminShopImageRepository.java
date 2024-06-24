package in.koreatech.koin.admin.shop.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.ShopImage;

public interface AdminShopImageRepository extends Repository<ShopImage, Integer> {

    ShopImage save(ShopImage shopImage);
}
