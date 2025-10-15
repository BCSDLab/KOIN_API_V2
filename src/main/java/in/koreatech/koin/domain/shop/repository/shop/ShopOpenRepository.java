package in.koreatech.koin.domain.shop.repository.shop;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.shop.ShopOpen;

public interface ShopOpenRepository extends Repository<ShopOpen, Integer> {

    ShopOpen save(ShopOpen shopOpen);

    List<ShopOpen> findAllByShopId(Integer shopId);
}
