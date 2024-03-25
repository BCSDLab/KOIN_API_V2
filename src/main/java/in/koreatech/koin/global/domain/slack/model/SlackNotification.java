package in.koreatech.koin.global.domain.slack.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SlackNotification {

    public static final String COLOR_GOOD = "good";

    private final String channel;
    private final String content;

    @Builder
    private SlackNotification(
        String channel,
        String text
    ) {
        this.channel = channel;
        this.content = text;
    }
}
