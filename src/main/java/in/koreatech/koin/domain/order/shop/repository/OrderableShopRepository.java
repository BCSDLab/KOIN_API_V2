package in.koreatech.koin.domain.order.shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.order.shop.exception.OrderableShopNotFoundException;
import in.koreatech.koin.domain.order.shop.model.domain.OrderableShopInfoSummary;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;

public interface OrderableShopRepository extends JpaRepository<OrderableShop, Integer> {

    @Query("""
            SELECT new in.koreatech.koin.domain.order.shop.model.domain.OrderableShopInfoSummary(
                s.id,
                os.id,
                s.name,
                s.introduction,
                os.delivery,
                os.takeout,
                os.shop.payCard,
                os.shop.payBank,
                os.minimumOrderAmount,
                CAST(COALESCE(AVG(r.rating), 0.0) AS double),
                CAST(COUNT(DISTINCT r.id) AS int),
                CAST(COALESCE(MIN(bdt.fee), 0) AS int),
                CAST(COALESCE(MAX(bdt.fee), 0) AS int),
                COALESCE(
                    (SELECT thumbnail.imageUrl
                     FROM OrderableShopImage thumbnail
                     WHERE thumbnail.orderableShop.id = os.id
                     AND thumbnail.isThumbnail = true
                     AND thumbnail.isDeleted = false),
                    (SELECT normal.imageUrl
                     FROM OrderableShopImage normal
                     WHERE normal.orderableShop.id = os.id
                     AND normal.isDeleted = false
                     ORDER BY normal.id ASC LIMIT 1
                    )
                )
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

    @Query("""
            SELECT DISTINCT os
            FROM OrderableShop os
            LEFT JOIN FETCH os.menuGroups mg
            WHERE os.id = :orderableShopId
        """)
    Optional<OrderableShop> findByIdWithMenus(@Param("orderableShopId") Integer orderableShopId);

    default OrderableShop getByIdWithMenus(Integer orderableShopId) {
        return findByIdWithMenus(orderableShopId).orElseThrow(
            () -> new OrderableShopNotFoundException(
                "해당 상점이 존재하지 않습니다 : " + orderableShopId
            )
        );
    }

    default OrderableShop getById(Integer shopId) {
        return findById(shopId).orElseThrow(
            () -> new OrderableShopNotFoundException(
                "해당 상점이 존재하지 않습니다 : " + shopId
            )
        );
    }
}
