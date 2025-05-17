package in.koreatech.koin.infrastructure.slack.eventlistener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.StudentRegisterRequestEvent;
import in.koreatech.koin._common.event.StudentRegisterEvent;
import in.koreatech.koin.infrastructure.slack.client.SlackClient;
import in.koreatech.koin.infrastructure.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class StudentEventListener {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onStudentRegisterRequestEvent(StudentRegisterRequestEvent event) {
        var notification = slackNotificationFactory.generateStudentEmailVerificationRequestNotification(event.email());
        slackClient.sendMessage(notification);
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onStudentRegisterEvent(StudentRegisterEvent event) {
        var notification = slackNotificationFactory.generateStudentRegisterCompleteNotification(event.email());
        slackClient.sendMessage(notification);
    }
}
