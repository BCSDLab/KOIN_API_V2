package in.koreatech.koin.domain.student.model;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.global.domain.notification.service.NotificationService;
import in.koreatech.koin.global.domain.slack.SlackClient;
import in.koreatech.koin.global.domain.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class StudentEventListener {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onStudentEmailRequest(StudentEmailRequestEvent event) {
        var notification = slackNotificationFactory.generateStudentEmailVerificationRequestNotification(event.email());
        slackClient.sendMessage(notification);
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onStudentRegister(StudentRegisterEvent event) {
        var notification = slackNotificationFactory.generateStudentRegisterCompleteNotification(event.email());
        slackClient.sendMessage(notification);
    }
}
