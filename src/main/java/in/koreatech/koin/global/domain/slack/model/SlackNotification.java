package in.koreatech.koin.global.domain.slack.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SlackNotification {

    private static final String EMAIL_VERIFICATION_REQUEST_SUFFIX = "님이 이메일 인증을 요청하였습니다.";
    private static final String CHANNEL_EVENT_NOTIFICATION = "#코인_이벤트알림";
    private static final String USERNAME_MEMBER_PLATFORM = "회원 플랫폼";
    private static final String COLOR_GOOD = "good";

    private final String channel;
    private final String username;
    private final List<Object> attachments;
    private final String color;
    private final String text;
    private final String url;

    public Map<String, Object> getParams() {
        Map<String, Object> params = new HashMap<>();

        if (getChannel() != null) {
            params.put("channel", getChannel());
        }
        if (getUsername() != null) {
            params.put("username", getUsername());
        }
        if (getAttachments() != null) {
            params.put("attachments", getAttachments());
        }

        return params;
    }

    public static SlackNotification noticeEmailVerification(String email, String url) {
        List<Object> attachment = List.of(addColor(COLOR_GOOD, email + EMAIL_VERIFICATION_REQUEST_SUFFIX));

        return SlackNotification.builder()
            .channel(CHANNEL_EVENT_NOTIFICATION)
            .username(USERNAME_MEMBER_PLATFORM)
            .attachments(attachment)
            .url(url)
            .build();
    }

    private static Map<String, Object> addColor(String color, String text) {
        Map<String, Object> params = new HashMap<>();
        params.put("color", color);
        params.put("text", text);

        return params;
    }
}
