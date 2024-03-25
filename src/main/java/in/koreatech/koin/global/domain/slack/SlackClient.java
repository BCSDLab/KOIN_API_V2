package in.koreatech.koin.global.domain.slack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.global.domain.slack.model.SlackNotification;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
public class SlackClient {

    private final String slackUrl;
    private final RestTemplate restTemplate;

    public SlackClient(
        @Value("${slack.notify_koin_event_url}") String slackUrl,
        RestTemplate restTemplate
    ) {
        this.slackUrl = slackUrl;
        this.restTemplate = restTemplate;
    }

    public void sendMessage(SlackNotification slackNotification) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        Map<String, Object> slackMessage = new HashMap<>();
        slackMessage.put("channel", slackNotification.getChannel());
        slackMessage.put("text", slackNotification.getContent());
        slackMessage.put("attachments", List.of(
            Map.of("color", SlackNotification.COLOR_GOOD)
        ));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(slackMessage, headers);
        restTemplate.postForObject(
            slackUrl,
            request,
            String.class
        );
    }
}
