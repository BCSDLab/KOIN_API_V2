package in.koreatech.koin.global.domain.slack.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.model.UserType;

@Component
public class SlackNotificationFactory {

    private static final String MARKDOWN_ADMIN_PAGE_URL_FORMAT = "<%s|Admin page 바로가기>";

    private final String adminPageUrl;
    private final String ownerEventNotificationUrl;
    private final String eventNotificationUrl;

    public SlackNotificationFactory(
        @Value("${koin.admin.url}") String adminPageUrl,
        @Value("${slack.koin_event_notify_url}") String eventNotificationUrl,
        @Value("${slack.koin_owner_event_notify_url}") String ownerEventNotificationUrl
    ) {
        this.adminPageUrl = adminPageUrl;
        this.eventNotificationUrl = eventNotificationUrl;
        this.ownerEventNotificationUrl = ownerEventNotificationUrl;
    }

    /**
     * 사장님 이메일 인증 요청 알림
     */
    public SlackNotification generateOwnerEmailVerificationRequestNotification(
        String content
    ) {
        return SlackNotification.builder()
            .slackUrl(ownerEventNotificationUrl)
            .text(String.format("""
                `%s(사장님)님이 이메일 인증을 요청하셨습니다.`
                """, content)
            )
            .build();
    }

    public SlackNotification generateOwnerPhoneVerificationRequestNotification(
        String content
    ) {
        return SlackNotification.builder()
            .slackUrl(ownerEventNotificationUrl)
            .text(String.format("""
                `%s(사장님)님이 문자 인증을 요청하셨습니다.`
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
            .slackUrl(ownerEventNotificationUrl)
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
            .slackUrl(ownerEventNotificationUrl)
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

    /**
     * 학생 이메일 인증 요청 알림
     */
    public SlackNotification generateStudentEmailVerificationRequestNotification(
        String content
    ) {
        return SlackNotification.builder()
            .slackUrl(eventNotificationUrl)
            .text(String.format("""
                `%s(학생)님이 이메일 인증을 요청하셨습니다.`
                """, content)
            )
            .build();
    }

    /**
     * 학생 가입 완료 알림
     */
    public SlackNotification generateStudentRegisterCompleteNotification(
        String content
    ) {
        return SlackNotification.builder()
            .slackUrl(eventNotificationUrl)
            .text(String.format("""
                `%s(학생)님이 가입하셨습니다.`
                """, content)
            )
            .build();
    }

    /**
     * 유저 탈퇴 알림
     */
    public SlackNotification generateUserDeleteNotification(
        String email,
        UserType userType
    ) {
        return SlackNotification.builder()
            .slackUrl(eventNotificationUrl)
            .text(String.format("""
                `%s(%s)님이 탈퇴하셨습니다.`
                """, email, userType.getDescription())
            )
            .build();
    }
}
