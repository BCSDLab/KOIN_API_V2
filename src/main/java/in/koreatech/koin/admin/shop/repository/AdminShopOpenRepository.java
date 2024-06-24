package in.koreatech.koin.admin.shop.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.ShopOpen;

public interface AdminShopOpenRepository extends Repository<ShopOpen, Integer> {

    ShopOpen save(ShopOpen shopOpen);
}
