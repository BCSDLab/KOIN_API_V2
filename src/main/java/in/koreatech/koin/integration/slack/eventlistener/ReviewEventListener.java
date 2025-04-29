package in.koreatech.koin.integration.slack.eventlistener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.ReviewRegisterEvent;
import in.koreatech.koin._common.event.ReviewReportEvent;
import in.koreatech.koin.integration.slack.client.SlackClient;
import in.koreatech.koin.integration.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ReviewEventListener {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onReviewRegister(ReviewRegisterEvent event) {
        var notification = slackNotificationFactory.generateReviewRegisterNotification(
            event.shop(),
            event.content(),
            event.rating()
        );
        slackClient.sendMessage(notification);
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onReviewReportRegister(ReviewReportEvent event) {
        var notification = slackNotificationFactory.generateReviewReportNotification(event.shop());
        slackClient.sendMessage(notification);
    }
}
