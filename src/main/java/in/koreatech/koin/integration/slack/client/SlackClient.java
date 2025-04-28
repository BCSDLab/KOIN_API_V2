package in.koreatech.koin.integration.slack.client;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.integration.slack.model.SlackNotification;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlackClient {

    private final RestTemplate restTemplate;

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
}
