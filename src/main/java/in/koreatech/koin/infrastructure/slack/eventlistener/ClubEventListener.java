package in.koreatech.koin.infrastructure.slack.eventlistener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.ClubCreateEvent;
import in.koreatech.koin.infrastructure.slack.client.SlackClient;
import in.koreatech.koin.infrastructure.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ClubEventListener {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @Async
    @TransactionalEventListener
    public void onClubCreateEvent(ClubCreateEvent event) {
        var notification = slackNotificationFactory.generateClubCreateSendNotification(event.clubName());
        slackClient.sendMessage(notification);
    }
}
