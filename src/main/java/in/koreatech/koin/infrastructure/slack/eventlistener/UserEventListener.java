package in.koreatech.koin.infrastructure.slack.eventlistener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.UserDeleteEvent;
import in.koreatech.koin._common.event.UserEmailVerificationSendEvent;
import in.koreatech.koin._common.event.UserSmsVerificationSendEvent;
import in.koreatech.koin.infrastructure.slack.client.SlackClient;
import in.koreatech.koin.infrastructure.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class UserEventListener {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onUserDeleteEvent(UserDeleteEvent event) {
        var notification = slackNotificationFactory.generateUserDeleteNotification(event.email(), event.userType());
        slackClient.sendMessage(notification);
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onUserSmsVerificationSendEvent(UserSmsVerificationSendEvent userSmsVerificationSendEvent) {
        var notification = slackNotificationFactory.generateUserPhoneVerificationSendNotification(
            userSmsVerificationSendEvent.phoneNumber());
        slackClient.sendMessage(notification);
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onUserEmailVerificationSendEvent(UserEmailVerificationSendEvent userEmailVerificationSendEvent) {
        var notification = slackNotificationFactory.generateUserEmailVerificationSendNotification(
            userEmailVerificationSendEvent.email());
        slackClient.sendMessage(notification);
    }
}
