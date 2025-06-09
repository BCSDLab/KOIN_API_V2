package in.koreatech.koin.infrastructure.slack.eventlistener;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.ReviewRegisterEvent;
import in.koreatech.koin._common.event.ReviewReportEvent;
import in.koreatech.koin.infrastructure.slack.client.SlackClient;
import in.koreatech.koin.infrastructure.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class ReviewEventListener {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @Async
    @TransactionalEventListener
    public void onReviewRegister(ReviewRegisterEvent event) {
        var notification = slackNotificationFactory.generateReviewRegisterNotification(
            event.shop(),
            event.content(),
            event.rating()
        );
        slackClient.sendMessage(notification);
    }

    @Async
    @TransactionalEventListener
    public void onReviewReportRegister(ReviewReportEvent event) {
        var notification = slackNotificationFactory.generateReviewReportNotification(event.shop());
        slackClient.sendMessage(notification);
    }
}
