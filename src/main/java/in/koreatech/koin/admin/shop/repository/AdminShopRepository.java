package in.koreatech.koin.admin.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.shop.exception.ShopNotFoundException;
import in.koreatech.koin.domain.shop.model.Shop;

public interface AdminShopRepository extends Repository<Shop, Integer> {

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

    List<Shop> findAll();

    @Modifying
    @Query("UPDATE Shop s SET s.isDeleted = true WHERE s.id = :shopId")
    int deleteById(@Param("shopId") Integer shopId);
}
