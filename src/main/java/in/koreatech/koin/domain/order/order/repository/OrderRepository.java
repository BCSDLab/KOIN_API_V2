package in.koreatech.koin.domain.order.order.repository;

import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_ORDER;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderStatus;
import in.koreatech.koin.global.exception.CustomException;

public interface OrderRepository extends Repository<Order, Integer> {

    void save(Order order);

    Optional<Order> findById(Integer orderId);

    default Order getById(Integer orderId) {
        return findById(orderId)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_ORDER));
    }

    @Query("""
        SELECT DISTINCT o
        FROM Order o
        JOIN FETCH o.orderableShop os
        LEFT JOIN FETCH o.orderDelivery od
        LEFT JOIN FETCH o.orderTakeout ot
        WHERE o.user.id = :userId
          AND o.status IN :statuses
        ORDER BY o.createdAt DESC
    """)
    List<Order> findAllByUserIdAndStatuses(
        @Param("userId") Integer userId,
        @Param("statuses") List<OrderStatus> statuses
    );

    @Query("""
        SELECT o
        FROM Order o
        LEFT JOIN FETCH o.orderDelivery od
        WHERE o.orderableShop.id = :orderableShopId
          AND o.status IN :statuses
        ORDER BY o.createdAt DESC
    """)
    List<Order> findAllByOrderableShopIdAndStatuses(
        @Param("orderableShopId") Integer orderableShopId,
        @Param("statuses") List<OrderStatus> statuses
    );

    long countByOrderableShopIdAndStatusIn(Integer orderableShopId, List<OrderStatus> statuses);

    @Query("""
        SELECT DISTINCT o
        FROM Order o
        JOIN FETCH o.orderableShop os
        LEFT JOIN FETCH o.orderDelivery od
        LEFT JOIN FETCH o.orderMenus om
        WHERE o.id = :orderId
          AND o.orderableShop.id = :orderableShopId
    """)
    Optional<Order> findByIdAndOrderableShopId(
        @Param("orderId") Integer orderId,
        @Param("orderableShopId") Integer orderableShopId
    );
}
