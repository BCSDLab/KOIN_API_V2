package in.koreatech.koin.domain.order.order.repository;

import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_ORDER;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.global.config.repository.JpaRepository;
import in.koreatech.koin.global.exception.CustomException;

@JpaRepository
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
          AND o.status NOT IN (
            in.koreatech.koin.domain.order.order.model.OrderStatus.DELIVERED,
            in.koreatech.koin.domain.order.order.model.OrderStatus.PICKED_UP,
            in.koreatech.koin.domain.order.order.model.OrderStatus.CANCELED
          )
        ORDER BY o.createdAt DESC
    """)
    List<Order> findOrderWithStatus(@Param("userId") Integer userId);
}
