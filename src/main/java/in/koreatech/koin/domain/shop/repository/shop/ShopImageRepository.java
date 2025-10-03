package in.koreatech.koin.domain.shop.repository.shop;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.shop.ShopImage;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface ShopImageRepository extends Repository<ShopImage, Integer> {

    ShopImage save(ShopImage shopImage);

    List<ShopImage> findAllByShopId(Integer shopId);
}
