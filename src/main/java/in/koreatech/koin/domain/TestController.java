package in.koreatech.koin.domain;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.global.domain.slack.SlackClient;
import in.koreatech.koin.global.domain.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @GetMapping("/test/slack")
    public ResponseEntity<Void> send() {
        var notification = slackNotificationFactory.generateOwnerEmailVerificationRequestNotification("너의 이메일");
        slackClient.sendMessage(notification);

        var notification2 = slackNotificationFactory.generateOwnerRegisterRequestNotification("최준호", "열라맛있어 가게");
        slackClient.sendMessage(notification2);

        var notification3 = slackNotificationFactory.generateOwnerEmailVerificationCompleteNotification("나의 이메일");
        slackClient.sendMessage(notification3);

        return ResponseEntity.ok().build();
    }
}
