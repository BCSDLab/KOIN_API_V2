package in.koreatech.koin.global.domain.slack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.domain.owner.model.OwnerInVerification;
import in.koreatech.koin.global.domain.slack.model.SlackNotification;

@Service
public class SlackService {

    @Value("${slack.notify_koin_event_url}")
    private String notify_koin_event_url;

    private final RestTemplate restTemplate = new RestTemplate();

    public void noticeEmailVerification(OwnerInVerification user) {
        SlackNotification slackNotification = SlackNotification.noticeEmailVerification(user, notify_koin_event_url);
        noticeFor(slackNotification);
    }

    private void noticeFor(SlackNotification slackNotification) {
        restTemplate.postForObject(slackNotification.getUrl(), slackNotification.getParams(), String.class);
    }
}
