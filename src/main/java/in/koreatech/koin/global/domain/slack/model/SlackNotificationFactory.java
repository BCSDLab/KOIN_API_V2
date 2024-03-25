package in.koreatech.koin.global.domain.slack.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SlackNotificationFactory {

    private static final String CHANNEL_EVENT_NOTIFICATION = "#코인_이벤트알림";
    private static final String CHANNEL_OWNER_EVENT_NOTIFICATION = "#코인_이벤트알림_사장님";
    private static final String MARKDOWN_ADMIN_PAGE_URL_FORMAT = "<%s|Admin page 바로가기>";

    private final String adminPageUrl;

    public SlackNotificationFactory(
        @Value("${slack.koin_admin_url}") String adminPageUrl
    ) {
        this.adminPageUrl = adminPageUrl;
    }

    /**
     * 사장님 이메일 인증 요청 알림
     */
    public SlackNotification generateOwnerEmailVerificationRequestNotification(
        String content
    ) {
        return SlackNotification.builder()
            .channel(CHANNEL_OWNER_EVENT_NOTIFICATION)
            .text(String.format("""
                `%s(사장님)님이 이메일 인증을 요청하셨습니다.`
                """, content)
            )
            .build();
    }

    /**
     * 사장님 이메일 인증 요청 알림
     */
    public SlackNotification generateflafjlasfNotificaiton(
        String content
    ) {
        return SlackNotification.builder()
            .channel(CHANNEL_OWNER_EVENT_NOTIFICATION)
            .text(String.format("""
                `%s(사장님)님이 이메일 인증을 요청하셨습니다.`
                """, content)
            )
            .build();
    }

    /**
     * 사장님 이메일 인증 완료 알림
     */
    public SlackNotification generateOwnerEmailVerificationCompleteNotification(
        String content
    ) {
        return SlackNotification.builder()
            .channel(CHANNEL_OWNER_EVENT_NOTIFICATION)
            .text(String.format("""
                `%s(사장님)님이 이메일 인증을 완료했습니다.`
                """, content)
            )
            .build();
    }

    /**
     * 사장님 회원가입 요청 알림
     */
    public SlackNotification generateOwnerRegisterRequestNotification(
        String ownerName,
        String shopName
    ) {
        return SlackNotification.builder()
            .channel(CHANNEL_OWNER_EVENT_NOTIFICATION)
            .text(String.format("""
                        `%s`님이 가입 승인을 기다리고 있어요!
                        가게 정보: `%s`
                        %s
                        """,
                    ownerName,
                    shopName,
                    String.format(
                        MARKDOWN_ADMIN_PAGE_URL_FORMAT,
                        adminPageUrl
                    )
                )
            )
            .build();
    }
}
