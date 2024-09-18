package in.koreatech.koin.admin.shop.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.shop.ShopOpen;

public interface AdminShopOpenRepository extends Repository<ShopOpen, Integer> {

    ShopOpen save(ShopOpen shopOpen);

    List<ShopOpen> findAllByShopId(Integer shopId);
}
