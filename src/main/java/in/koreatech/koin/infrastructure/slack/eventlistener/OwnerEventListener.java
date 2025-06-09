package in.koreatech.koin.infrastructure.slack.eventlistener;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.OwnerRegisterEvent;
import in.koreatech.koin._common.event.OwnerSmsVerificationSendEvent;
import in.koreatech.koin.infrastructure.slack.client.SlackClient;
import in.koreatech.koin.infrastructure.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class OwnerEventListener {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @Async
    @TransactionalEventListener
    public void onOwnerPhoneRequest(OwnerSmsVerificationSendEvent ownerPhoneRequestEvent) {
        var notification = slackNotificationFactory.generateOwnerPhoneVerificationRequestNotification(
            ownerPhoneRequestEvent.phoneNumber());
        slackClient.sendMessage(notification);
    }

    @Async
    @TransactionalEventListener
    public void onOwnerRegisterBySms(OwnerRegisterEvent event) {
        var notification = slackNotificationFactory.generateOwnerRegisterRequestNotification(
            event.ownerName(),
            event.shopName()
        );
        slackClient.sendMessage(notification);
    }
}
