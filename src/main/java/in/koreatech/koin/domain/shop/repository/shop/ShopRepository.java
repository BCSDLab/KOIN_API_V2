package in.koreatech.koin.domain.shop.repository.shop;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.ShopNotFoundException;
import in.koreatech.koin.domain.shop.model.shop.Shop;

public interface ShopRepository extends Repository<Shop, Integer> {

    Shop save(Shop shop);

    List<Shop> findAllByOwnerId(Integer ownerId);

    Optional<Shop> findById(Integer shopId);

    Optional<Shop> findByOwnerId(Integer ownerId);

    default Shop getById(Integer shopId) {
        return findById(shopId)
            .orElseThrow(() -> ShopNotFoundException.withDetail("shopId: " + shopId));
    }

    default Shop getByOwnerId(Integer ownerId) {
        return findByOwnerId(ownerId)
            .orElseThrow(() -> ShopNotFoundException.withDetail("ownerId: " + ownerId));
    }

    @EntityGraph(value = "Shop.withAll", type = EntityGraph.EntityGraphType.LOAD)
    List<Shop> findAll();

}
