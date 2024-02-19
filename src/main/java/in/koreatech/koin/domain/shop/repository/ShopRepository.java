package in.koreatech.koin.domain.shop.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.Shop;

public interface ShopRepository extends Repository<Shop, Long> {

    Shop save(Shop shop);

    List<Shop> findAllByOwnerId(Long ownerId);
}
