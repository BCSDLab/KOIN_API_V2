package in.koreatech.koin.unit.domain.notification.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.common.model.MobileAppPath;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.notification.i18n.NotificationMessages;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.callvan.model.CallvanNotification;
import in.koreatech.koin.domain.callvan.model.CallvanPushNotificationFactory;
import in.koreatech.koin.domain.callvan.model.enums.CallvanNotificationType;

class NotificationMessagesTest {
    @Test
    void 모든_지원언어에서_시스템문구만_번역하고_삽입값과_미등록문구는_보존한다() {
        String nickname = "원문%닉네임\n東京";
        for (SupportedLanguage language : SupportedLanguage.values()) {
            User user = user(language.getCode());
            String message = NotificationMessages.format(user, "%s님의 메시지", nickname);
            assertThat(message).contains(nickname);
            if (language != SupportedLanguage.KOREAN) {
                assertThat(message).isNotEqualTo(nickname + "님의 메시지");
            }
            assertThat(NotificationMessages.translateKnown(user, "미등록 관리자 문구 100%"))
                .isEqualTo("미등록 관리자 문구 100%");
        }
        assertThat(NotificationMessages.format(user(null), "%s님의 메시지", nickname))
            .isEqualTo(nickname + "님의 메시지");
        assertThat(NotificationMessages.translateKnown(user("en-US"), "콜벤팟 원문님의 메시지"))
            .isEqualTo("콜벤팟 원문님의 메시지");
        String title = "지원하신 X 모집이 마감되어";
        assertThat(NotificationMessages.teamMessage(user("en-US"),
            in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.APPLICATION_REJECTED,
            title, title + " 지원이 거절되었어요."))
            .isEqualTo("Your application to " + title + " was declined.");
        assertThat(NotificationMessages.teamMessage(user("en-US"),
            in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.APPLICATION_REJECTED,
            "수정된 제목", title + " 지원이 거절되었어요."))
            .isEqualTo(title + " 지원이 거절되었어요.");
    }

    @Test
    void 일반푸시의_사용자쪽지와_제목은_시스템문구와_같아도_번역하지_않는다() {
        var factory = new NotificationFactory();
        User user = user("en-US");
        String userText = "주문이 접수되었어요";
        var chat = factory.generateChatMessageNotification(MobileAppPath.SHOP, 1, 2, "닉네임", userText, user);
        assertThat(chat.getTitle()).isEqualTo("Message from 닉네임");
        assertThat(chat.getMessage()).isEqualTo(userText);
        var keyword = factory.generateKeywordNotification(MobileAppPath.SHOP, 1, "장학금", userText, 4, user);
        assertThat(keyword.getTitle()).isEqualTo(userText);
        assertThat(keyword.getMessage()).isEqualTo("Check the new notice for 장학금!");
        var review = factory.generateReviewPromptNotification(MobileAppPath.SHOP, 1, "원문상호%",
            ",맛있게 드셨나요?", "가게에 리뷰를 남겨보세요!", user);
        assertThat(review.getTitle()).isEqualTo("원문상호%, did you enjoy your meal?");
        assertThat(review.getMessage()).isEqualTo("Leave a review for the shop!");
        var customReview = factory.generateReviewPromptNotification(MobileAppPath.SHOP, 1, "원문상호%",
            ",새로운 안내", "자유작성 안내문 100%", user);
        assertThat(customReview.getTitle()).isEqualTo("원문상호%,새로운 안내");
        assertThat(customReview.getMessage()).isEqualTo("자유작성 안내문 100%");
    }

    @Test
    void 콜벤_서비스알림은_조회언어를_따르고_쪽지는_원문을_유지한다() {
        User user = user("en-US");
        CallvanNotification notification = mock(CallvanNotification.class);
        when(notification.getRecipient()).thenReturn(user);
        when(notification.getNotificationType()).thenReturn(CallvanNotificationType.RECRUITMENT_COMPLETE);
        when(notification.getMessagePreview()).thenReturn("해당 콜벤팟 인원이 모두 모집되었습니다. 콜벤을 예약하세요");
        assertThat(CallvanPushNotificationFactory.localizedPreview(notification))
            .isEqualTo("The Callvan group is full. Please book the ride.");
        when(notification.getNotificationType()).thenReturn(CallvanNotificationType.NEW_MESSAGE);
        assertThat(CallvanPushNotificationFactory.localizedPreview(notification))
            .isEqualTo("해당 콜벤팟 인원이 모두 모집되었습니다. 콜벤을 예약하세요");
        when(notification.getNotificationType()).thenReturn(CallvanNotificationType.DEPARTURE_UPCOMING);
        when(notification.getDepartureCustomName()).thenReturn("A -> B");
        when(notification.getArrivalCustomName()).thenReturn("C");
        when(notification.getDepartureType()).thenReturn(
            in.koreatech.koin.domain.callvan.model.enums.CallvanLocation.values()[0]);
        when(notification.getArrivalType()).thenReturn(
            in.koreatech.koin.domain.callvan.model.enums.CallvanLocation.values()[0]);
        assertThat(CallvanPushNotificationFactory.localizedPreview(notification))
            .isEqualTo("Callvan from A -> B to C departs in 30 minutes");
    }

    private User user(String language) {
        User user = mock(User.class);
        when(user.getPreferredLanguage()).thenReturn(language);
        return user;
    }
}
