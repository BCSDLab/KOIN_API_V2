package in.koreatech.koin.domain.shop.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.ShopOpen;

public interface ShopOpenRepository extends Repository<ShopOpen, Integer> {

    ShopOpen save(ShopOpen shopOpen);

    List<ShopOpen> findAllByShopId(Integer shopId);
}
