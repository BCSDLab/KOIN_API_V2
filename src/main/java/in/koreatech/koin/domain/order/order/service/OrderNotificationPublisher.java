package in.koreatech.koin.domain.order.order.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import in.koreatech.koin.common.event.OrderNotificationEvent;
import in.koreatech.koin.domain.order.order.model.Order;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderNotificationPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(Order order) {
        eventPublisher.publishEvent(OrderNotificationEvent.of(
            order.getId(),
            order.getUser().getId(),
            order.getOrderableShopName(),
            order.getStatus().name(),
            order.isDelivery(),
            order.getEstimatedAt()
        ));
    }
}
