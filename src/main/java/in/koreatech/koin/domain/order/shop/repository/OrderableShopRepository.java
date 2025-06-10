package in.koreatech.koin.domain.order.shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.order.shop.model.entity.OrderableShop;

public interface OrderableShopRepository extends JpaRepository<OrderableShop, Integer> {

    @Query("""
         SELECT os
         FROM OrderableShop os
         JOIN FETCH os.shop
         JOIN FETCH os.shop.reviews
         WHERE os.id =:shopId
    """)
    Optional<OrderableShop> findById(@Param("shopId") Integer shopId);
}
