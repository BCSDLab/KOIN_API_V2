package in.koreatech.koin.domain.order.order.event;

import java.time.LocalDateTime;

import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderStatus;
import in.koreatech.koin.domain.order.order.model.OrderType;

public record OrderStatusChangedEvent(
    Integer orderId,
    Integer userId,
    String shopName,
    OrderStatus previousStatus,
    OrderStatus status,
    OrderType orderType,
    LocalDateTime estimatedAt
) {

    public static OrderStatusChangedEvent of(Order order, OrderStatus previousStatus) {
        return new OrderStatusChangedEvent(
            order.getId(),
            order.getUser().getId(),
            order.getOrderableShopName(),
            previousStatus,
            order.getStatus(),
            order.getOrderType(),
            order.getEstimatedAt()
        );
    }
}
