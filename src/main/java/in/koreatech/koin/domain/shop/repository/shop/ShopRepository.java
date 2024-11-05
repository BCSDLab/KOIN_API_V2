package in.koreatech.koin.domain.shop.repository.shop;

import in.koreatech.koin.domain.shop.exception.ShopNotFoundException;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

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

    List<Shop> findAll();

    @Query("SELECT DISTINCT s.name FROM Shop s WHERE s.name LIKE %:prefix%")
    List<String> findDistinctNameStartingWith(@Param("prefix") String prefix);
}
