package in.koreatech.koin.infrastructure.slack.eventlistener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.OwnerRegisterEvent;
import in.koreatech.koin._common.event.OwnerSmsRequestEvent;
import in.koreatech.koin.infrastructure.slack.client.SlackClient;
import in.koreatech.koin.infrastructure.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class OwnerEventListener {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onOwnerPhoneRequest(OwnerSmsRequestEvent ownerPhoneRequestEvent) {
        var notification = slackNotificationFactory.generateOwnerPhoneVerificationRequestNotification(
            ownerPhoneRequestEvent.phoneNumber());
        slackClient.sendMessage(notification);
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onOwnerRegisterBySms(OwnerRegisterEvent event) {
        var notification = slackNotificationFactory.generateOwnerRegisterRequestNotification(
            event.ownerName(),
            event.shopName()
        );
        slackClient.sendMessage(notification);
    }
}
