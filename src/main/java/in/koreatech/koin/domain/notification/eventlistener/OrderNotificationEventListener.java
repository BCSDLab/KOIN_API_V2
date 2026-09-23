package in.koreatech.koin.domain.notification.eventlistener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.OrderNotificationEvent;
import in.koreatech.koin.domain.notification.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderNotificationEventListener {

    private final OrderNotificationService orderNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderNotification(OrderNotificationEvent event) {
        orderNotificationService.pushNotification(event);
    }
}
