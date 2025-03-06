package in.koreatech.koin.integration.slack.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.model.UserType;

@Component
public class SlackNotificationFactory {

    private static final String MARKDOWN_ADMIN_PAGE_URL_FORMAT = "<%s|Admin page 바로가기>";

    private final String adminPageUrl;
    private final String adminReviewPageUrl;
    private final String ownerEventNotificationUrl;
    private final String eventNotificationUrl;
    private final String reviewNotificationUrl;
    private final String lostItemNotificationUrl;

    public SlackNotificationFactory(
        @Value("${koin.admin.shop.url}") String adminPageUrl,
        @Value("${koin.admin.review.url}") String adminReviewPageUrl,
        @Value("${slack.koin_event_notify_url}") String eventNotificationUrl,
        @Value("${slack.koin_owner_event_notify_url}") String ownerEventNotificationUrl,
        @Value("${slack.koin_shop_review_notify_url}") String reviewNotificationUrl,
        @Value("${slack.koin_lost_item_notify_url}") String lostItemNotificationUrl
    ) {
        this.adminPageUrl = adminPageUrl;
        this.adminReviewPageUrl = adminReviewPageUrl;
        this.eventNotificationUrl = eventNotificationUrl;
        this.ownerEventNotificationUrl = ownerEventNotificationUrl;
        this.reviewNotificationUrl = reviewNotificationUrl;
        this.lostItemNotificationUrl = lostItemNotificationUrl;
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

    /**
     * 상점 리뷰 등록 알림
     */
    public SlackNotification generateReviewRegisterNotification(
        String shop,
        String content,
        Integer rating
    ) {
        return SlackNotification.builder()
            .slackUrl(reviewNotificationUrl)
            .text(String.format("""
                `%s에 새로운 리뷰가 등록되었습니다.`
                내용: `%s`
                별점: `%d`
                %s
                """,
                shop,
                content,
                rating,
                String.format(
                    MARKDOWN_ADMIN_PAGE_URL_FORMAT,
                    adminReviewPageUrl
                ))
            )
            .build();
    }

    /**
     * 상점 리뷰 신고 알림
     */
    public SlackNotification generateReviewReportNotification(
        String shop
    ) {
        return SlackNotification.builder()
            .slackUrl(reviewNotificationUrl)
            .text(String.format("""
                `%s의 리뷰가 신고되었습니다.`
                `신고를 처리해주세요!!`
                %s
                """,
                shop,
                String.format(
                    MARKDOWN_ADMIN_PAGE_URL_FORMAT,
                    adminReviewPageUrl
                ))
            )
            .build();
    }

    /**
     * 분실물 게시글 신고 알림
     */
    public SlackNotification generateLostItemReportNotification(
        Integer lostItemArticleId
    ) {
        return SlackNotification.builder()
            .slackUrl(lostItemNotificationUrl)
            .text(String.format("""
                `%d번 분실물 게시글이 신고되었습니다.`
                `신고를 처리해주세요!!`
                %s
                """,
                lostItemArticleId,
                String.format(
                    MARKDOWN_ADMIN_PAGE_URL_FORMAT,
                    adminPageUrl
                ))
            )
            .build();
    }
}
