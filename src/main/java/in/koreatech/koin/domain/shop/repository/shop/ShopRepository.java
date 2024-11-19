package in.koreatech.koin.domain.shop.repository.shop;

import in.koreatech.koin.domain.shop.exception.ShopNotFoundException;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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

    @Query("""
                SELECT s.id, 
                       CASE WHEN COUNT(e.id) > 0 THEN true ELSE false END 
                FROM Shop s
                LEFT JOIN s.eventArticles e
                ON e.startDate <= :now AND e.endDate >= :now
                GROUP BY s.id
            """)
    List<Object[]> findAllShopEventStatus(@Param("now") LocalDate now);

    default Map<Integer, Boolean> getAllShopEventDuration(LocalDate now) {
        return findAllShopEventStatus(now).stream()
                .collect(Collectors.toMap(
                        result -> (Integer) result[0],
                        result -> (Boolean) result[1]
                ));
    }
}
