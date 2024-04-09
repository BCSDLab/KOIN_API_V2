package in.koreatech.koin.domain.coop.model;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.global.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class CoopEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onDiningSoldOutRequest(DiningSoldOutEvent event) {
        notificationService.pushSoldOutNotification(event);
    }
}
