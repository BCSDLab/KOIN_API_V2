package in.koreatech.koin.domain.order.order.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import in.koreatech.koin.common.event.OrderNotificationEvent;
import in.koreatech.koin.common.event.OrderNotificationType;
import in.koreatech.koin.domain.order.order.model.Order;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderNotificationPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(Order order) {
        OrderNotificationType type = switch (order.getStatus()) {
            case COOKING -> order.isDelivery()
                ? OrderNotificationType.ACCEPTED_DELIVERY
                : OrderNotificationType.ACCEPTED_TAKEOUT;
            case DELIVERING -> OrderNotificationType.DELIVERY_STARTED;
            case PACKAGED -> OrderNotificationType.PACKAGING_COMPLETED;
            case PICKED_UP -> OrderNotificationType.PICKED_UP;
            case DELIVERED -> OrderNotificationType.DELIVERED;
            case CANCELED -> OrderNotificationType.CANCELED;
            case CONFIRMING -> throw new IllegalArgumentException("주문 확인중 상태는 알림 대상이 아닙니다.");
        };

        eventPublisher.publishEvent(OrderNotificationEvent.of(
            order.getId(),
            order.getUser().getId(),
            order.getOrderableShopName(),
            type,
            order.getEstimatedAt()
        ));
    }
}
