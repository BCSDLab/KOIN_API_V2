package in.koreatech.koin.domain.order.order.service;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import in.koreatech.koin.common.event.OrderNotificationEvent;
import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderStatus;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderNotificationPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(Order order) {
        String message = switch (order.getStatus()) {
            case COOKING -> "주문이 접수되어 조리를 시작했어요.";
            case DELIVERING -> "주문하신 음식의 배달이 시작됐어요.";
            case PACKAGED -> "포장이 완료됐어요. 매장에서 주문을 수령해 주세요.";
            case PICKED_UP -> "주문 수령이 완료됐어요. 맛있게 드세요!";
            case DELIVERED -> "배달이 완료됐어요. 맛있게 드세요!";
            case CANCELED -> "주문이 취소됐어요. 자세한 내용은 주문 내역을 확인해 주세요.";
            case CONFIRMING -> throw new IllegalArgumentException("주문 확인중 상태는 알림 대상이 아닙니다.");
        };

        String estimatedTimeLabel = null;
        LocalDateTime estimatedAt = null;
        if (order.getStatus() == OrderStatus.COOKING || order.getStatus() == OrderStatus.DELIVERING) {
            estimatedTimeLabel = order.isDelivery() ? "예상 도착" : "포장 완료 예정";
            estimatedAt = order.getEstimatedAt();
        }

        eventPublisher.publishEvent(new OrderNotificationEvent(
            order.getId(),
            order.getUser().getId(),
            order.getOrderableShopName(),
            message,
            estimatedTimeLabel,
            estimatedAt
        ));
    }
}
