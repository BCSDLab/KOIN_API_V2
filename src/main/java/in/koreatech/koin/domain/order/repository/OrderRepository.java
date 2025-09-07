package in.koreatech.koin.domain.order.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.order.model.Order;

public interface OrderRepository extends Repository<Order, String> {

    void save(Order order);

    @Query("""
        SELECT DISTINCT o
        FROM Order o
        JOIN FETCH o.orderableShop os
        JOIN FETCH os.shop s
        LEFT JOIN FETCH o.orderDelivery od
        LEFT JOIN FETCH o.orderTakeout ot
        WHERE o.user.id = :userId
          AND o.status NOT IN (
            in.koreatech.koin.domain.order.model.OrderStatus.DELIVERED,
            in.koreatech.koin.domain.order.model.OrderStatus.CANCELED
          )
        ORDER BY o.createdAt DESC
    """)
    List<Order> findOrderWithStatus(@Param("userId") Integer userId);

    @Query("""
    SELECT DISTINCT o
    FROM Order o
    JOIN FETCH o.orderableShop os
    JOIN FETCH os.shop s
    LEFT JOIN FETCH o.orderDelivery od
    LEFT JOIN FETCH o.orderTakeout ot
    WHERE o.user.id = :userId
      AND o.status NOT IN (
        in.koreatech.koin.domain.order.model.OrderStatus.DELIVERED,
        in.koreatech.koin.domain.order.model.OrderStatus.CANCELED
      )
    ORDER BY o.createdAt DESC
""")
    List<Order> findOrderWithStatus(@Param("userId") Integer userId,
        Pageable pageable);
}
