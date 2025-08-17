package in.koreatech.koin.infrastructure.slack.eventlistener;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.StudentRegisterEvent;
import in.koreatech.koin.common.event.StudentRegisterRequestEvent;
import in.koreatech.koin.infrastructure.slack.client.SlackClient;
import in.koreatech.koin.infrastructure.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class StudentEventListener {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @Async
    @TransactionalEventListener
    public void onStudentRegisterRequestEvent(StudentRegisterRequestEvent event) {
        var notification = slackNotificationFactory.generateStudentEmailVerificationRequestNotification(event.email());
        slackClient.sendMessage(notification);
    }

    @Async
    @TransactionalEventListener
    public void onStudentRegisterEvent(StudentRegisterEvent event) {
        var notification = slackNotificationFactory.generateStudentRegisterCompleteNotification(event.phoneNumber());
        slackClient.sendMessage(notification);
    }
}
