package in.koreatech.koin.domain.shop.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.Shop;

public interface ShopRepository extends Repository<Shop, Long> {

    Shop save(Shop shop);
}
