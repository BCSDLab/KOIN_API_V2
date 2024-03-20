package in.koreatech.koin.global.domain.slack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.global.domain.slack.model.SlackNotification;

@Service
public class SlackService {

    @Value("${slack.notify_koin_event_url}")
    private String slackUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void noticeEmailVerification(String email) {
        SlackNotification slackNotification = SlackNotification.noticeEmailVerification(email, slackUrl);
        noticeFor(slackNotification);
    }

    private void noticeFor(SlackNotification slackNotification) {
        restTemplate.postForObject(slackNotification.getUrl(), slackNotification.getParams(), String.class);
    }
}
