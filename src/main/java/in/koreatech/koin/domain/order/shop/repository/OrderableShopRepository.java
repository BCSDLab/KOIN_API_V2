package in.koreatech.koin.domain.order.shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.order.shop.exception.OrderableShopNotFoundException;
import in.koreatech.koin.domain.order.shop.model.domain.OrderableShopInfoSummary;
import in.koreatech.koin.domain.order.shop.model.entity.OrderableShop;

public interface OrderableShopRepository extends JpaRepository<OrderableShop, Integer> {

    @Query("""
            SELECT new in.koreatech.koin.domain.order.shop.model.domain.OrderableShopInfoSummary(
                s.id,
                os.id,
                s.name,
                s.introduction,
                os.delivery,
                os.takeout,
                os.minimumOrderAmount,
                CAST(COALESCE(AVG(r.rating), 0.0) AS double),
                CAST(COUNT(DISTINCT r.id) AS int),
                CAST(COALESCE(MIN(bdt.fee), 0) AS int),
                CAST(COALESCE(MAX(bdt.fee), 0) AS int)
            )
            FROM OrderableShop os
            JOIN os.shop s
            LEFT JOIN s.reviews r
            LEFT JOIN ShopBaseDeliveryTip bdt ON bdt.shop.id = s.id
            WHERE os.id = :orderableShopId
            GROUP BY os.id, s.id
        """)
    Optional<OrderableShopInfoSummary> findOrderableShopInfoSummaryById(
        @Param("orderableShopId") Integer orderableShopId);

    default OrderableShopInfoSummary getOrderableShopInfoSummaryById(Integer shopId) {
        return findOrderableShopInfoSummaryById(shopId).orElseThrow(
            () -> new OrderableShopNotFoundException(
                "해당 상점이 존재하지 않습니다 : " + shopId
            )
        );
    }

    @Query("""
            SELECT os
            FROM OrderableShop os
            JOIN FETCH os.shop
            LEFT JOIN FETCH os.shop.owner
            LEFT JOIN FETCH os.shop.owner.user
            LEFT JOIN FETCH os.shop.shopOpens
            WHERE os.id = :orderableShopId
        """)
    Optional<OrderableShop> findByIdJoinFetchShop(@Param("orderableShopId") Integer orderableShopId);

    default OrderableShop getOrderableShopInfoDetailById(Integer orderableShopId) {
        return findByIdJoinFetchShop(orderableShopId).orElseThrow(
            () -> new OrderableShopNotFoundException(
                "해당 상점이 존재하지 않습니다 : " + orderableShopId
            )
        );
    }
}
