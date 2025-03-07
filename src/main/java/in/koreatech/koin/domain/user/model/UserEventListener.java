package in.koreatech.koin.domain.user.model;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.UserDeleteEvent;
import in.koreatech.koin.integration.slack.SlackClient;
import in.koreatech.koin.integration.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class UserEventListener {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onUserDeleteEvent(UserDeleteEvent event) {
        var notification = slackNotificationFactory.generateUserDeleteNotification(event.email(), event.userType());
        slackClient.sendMessage(notification);
    }
}
