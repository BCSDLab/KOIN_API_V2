package in.koreatech.koin.infrastructure.slack.eventlistener;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.AdminRegisterEvent;
import in.koreatech.koin.infrastructure.slack.client.SlackClient;
import in.koreatech.koin.infrastructure.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class AdminEventListener {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @Async
    @TransactionalEventListener
    public void onAdminRegisterEvent(AdminRegisterEvent event) {
        var notification = slackNotificationFactory.generateAdminCreateSendNotification(
            event.creatorId(),
            event.creatorName(),
            event.newAdminId(),
            event.newAdminName()
        );
        slackClient.sendMessage(notification);
    }
}
