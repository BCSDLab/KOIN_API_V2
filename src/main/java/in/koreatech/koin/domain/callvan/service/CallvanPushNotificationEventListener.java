package in.koreatech.koin.domain.callvan.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.callvan.event.CallvanPushNotificationEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CallvanPushNotificationEventListener {

    private final CallvanPushNotificationService callvanPushNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCallvanPushRequested(CallvanPushNotificationEvent event) {
        callvanPushNotificationService.pushNotifications(event.notificationIds());
    }
}
