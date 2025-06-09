package in.koreatech.koin.infrastructure.slack.eventlistener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.UserDeleteEvent;
import in.koreatech.koin._common.event.UserEmailVerificationSendEvent;
import in.koreatech.koin._common.event.UserSmsVerificationSendEvent;
import in.koreatech.koin.infrastructure.slack.client.SlackClient;
import in.koreatech.koin.infrastructure.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @Async
    @TransactionalEventListener
    public void onUserDeleteEvent(UserDeleteEvent event) {
        var notification = slackNotificationFactory.generateUserDeleteNotification(event.email(), event.userType());
        slackClient.sendMessage(notification);
    }

    @Async
    @TransactionalEventListener
    public void onUserSmsVerificationSendEvent(UserSmsVerificationSendEvent userSmsVerificationSendEvent) {
        var notification = slackNotificationFactory.generateUserPhoneVerificationSendNotification(
            userSmsVerificationSendEvent.phoneNumber());
        slackClient.sendMessage(notification);
    }

    @Async
    @TransactionalEventListener
    public void onUserEmailVerificationSendEvent(UserEmailVerificationSendEvent userEmailVerificationSendEvent) {
        var notification = slackNotificationFactory.generateUserEmailVerificationSendNotification(
            userEmailVerificationSendEvent.email());
        slackClient.sendMessage(notification);
    }
}
