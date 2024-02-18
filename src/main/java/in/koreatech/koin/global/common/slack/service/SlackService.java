package in.koreatech.koin.global.common.slack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.domain.owner.model.OwnerInVerification;
import in.koreatech.koin.global.common.slack.model.Notice;

@Service
public class SlackService {

    @Value("${slack.notify_koin_event_url}")
    private String notify_koin_event_url;

    private final RestTemplate restTemplate = new RestTemplate();

    public void noticeEmailVerification(OwnerInVerification user) {
        Notice notice = Notice.noticeEmailVerification(user, notify_koin_event_url);
        noticeFor(notice);
    }

    private void noticeFor(Notice notice) {
        restTemplate.postForObject(notice.getUrl(), notice.getParams(), String.class);
    }
}
