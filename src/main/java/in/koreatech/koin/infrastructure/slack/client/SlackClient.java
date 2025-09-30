package in.koreatech.koin.infrastructure.slack.client;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.infrastructure.slack.model.SlackNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackClient {

    private final RestTemplate restTemplate;

    @Retryable
    public void sendMessage(SlackNotification slackNotification) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        Map<String, Object> slackMessage = new HashMap<>();
        slackMessage.put("text", slackNotification.getContent());
        slackMessage.put("attachments", List.of(
            Map.of("color", SlackNotification.COLOR_GOOD)
        ));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(slackMessage, headers);
        restTemplate.postForObject(
            slackNotification.getSlackUrl(),
            request,
            String.class
        );
    }

    @Recover
    public void slackRecovery(Exception e, SlackNotification slackNotification) {
        log.error("슬랙 메시지 전송에 실패했습니다. content: {}", slackNotification.getContent(), e);
    }
}
