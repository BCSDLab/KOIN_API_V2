package in.koreatech.koin.integration.slack.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SlackNotification {

    public static final String COLOR_GOOD = "good";

    private final String slackUrl;
    private final String content;

    @Builder
    private SlackNotification(
        String slackUrl,
        String text
    ) {
        this.slackUrl = slackUrl;
        this.content = text;
    }
}
