package in.koreatech.koin.infrastructure.slack.eventlistener;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.LostItemReportEvent;
import in.koreatech.koin.infrastructure.slack.client.SlackClient;
import in.koreatech.koin.infrastructure.slack.model.SlackNotification;
import in.koreatech.koin.infrastructure.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class LostItemReportEventListener {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @Async
    @TransactionalEventListener
    public void onLostItemReportRegister(LostItemReportEvent event) {
        SlackNotification notification = slackNotificationFactory.generateLostItemReportNotification(
            event.lostItemArticleId());
        slackClient.sendMessage(notification);
    }
}
