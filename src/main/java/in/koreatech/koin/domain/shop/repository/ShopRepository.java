package in.koreatech.koin.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.ShopNotFoundException;
import in.koreatech.koin.domain.shop.model.Shop;

public interface ShopRepository extends Repository<Shop, Long> {

    Shop save(Shop shop);

    List<Shop> findAllByOwnerId(Integer ownerId);

    Optional<Shop> findById(Long shopId);

    Optional<Shop> findByOwnerId(Integer ownerId);

    default Shop getById(Long shopId) {
        return findById(shopId)
            .orElseThrow(() -> ShopNotFoundException.withDetail("shopId: " + shopId));
    }

    default Shop getByOwnerId(Integer ownerId) {
        return findByOwnerId(ownerId)
            .orElseThrow(() -> ShopNotFoundException.withDetail("ownerId: " + ownerId));
    }

    List<Shop> findAll();
}
