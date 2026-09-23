package in.koreatech.koin.domain.notification.eventlistener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.notification.service.OrderNotificationService;
import in.koreatech.koin.domain.order.order.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationEventListener {

    private final OrderNotificationService orderNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        try {
            orderNotificationService.pushNotification(event);
        } catch (Exception e) {
            log.error("주문 상태 알림 처리 실패. orderId={}, userId={}, status={}",
                event.orderId(), event.userId(), event.status(), e);
        }
    }
}
