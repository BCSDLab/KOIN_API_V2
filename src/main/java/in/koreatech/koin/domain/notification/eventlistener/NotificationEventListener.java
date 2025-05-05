package in.koreatech.koin.domain.notification.eventlistener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.UserRegisterEvent;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class NotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onUserRegisterEvent(UserRegisterEvent event) {
        if (event.marketingNotificationAgreement()) {
            notificationService.permitNotificationSubscribe(event.userId(), NotificationSubscribeType.MARKETING);
        }
    }
}
