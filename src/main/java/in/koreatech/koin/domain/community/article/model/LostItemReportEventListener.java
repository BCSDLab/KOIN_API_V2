package in.koreatech.koin.domain.community.article.model;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.LostItemReportEvent;
import in.koreatech.koin.integration.slack.SlackClient;
import in.koreatech.koin.integration.slack.model.SlackNotification;
import in.koreatech.koin.integration.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = REQUIRES_NEW)
public class LostItemReportEventListener {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onLostItemReportRegister(LostItemReportEvent event) {
        SlackNotification notification = slackNotificationFactory.generateLostItemReportNotification(
            event.lostItemArticleId());
        slackClient.sendMessage(notification);
    }
}
