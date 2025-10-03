package in.koreatech.koin.domain.order.shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.order.shop.model.domain.OrderableShopInfoSummary;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

@in.koreatech.koin.global.config.repository.JpaRepository
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

    default OrderableShopInfoSummary getOrderableShopInfoSummaryById(Integer orderableShopId) {
        return findOrderableShopInfoSummaryById(orderableShopId).orElseThrow(
            () -> CustomException.of(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP, "해당 상점이 존재하지 않습니다 : " + orderableShopId)
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
            () -> CustomException.of(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP, "해당 상점이 존재하지 않습니다 : " + orderableShopId)
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
            () -> CustomException.of(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP, "해당 상점이 존재하지 않습니다 : " + orderableShopId)
        );
    }

    default OrderableShop getById(Integer orderableShopId) {
        return findById(orderableShopId).orElseThrow(
            () -> CustomException.of(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP, "해당 상점이 존재하지 않습니다 : " + orderableShopId)
        );
    }
}
