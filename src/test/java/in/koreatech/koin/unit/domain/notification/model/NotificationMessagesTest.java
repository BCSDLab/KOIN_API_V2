package in.koreatech.koin.unit.domain.notification.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.common.model.MobileAppPath;
import in.koreatech.koin.domain.notification.i18n.NotificationMessages;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType;
import in.koreatech.koin.domain.user.model.User;

class NotificationMessagesTest {
    @Test
    void 미설정_언어와_미등록_문구는_원문으로_대체한다() {
        User user = mock(User.class);
        String template = "%s님의 메시지";
        assertThat(NotificationMessages.format(null, template, "닉네임")).isEqualTo("닉네임님의 메시지");
        assertThat(NotificationMessages.format(user, template, "닉네임")).isEqualTo("닉네임님의 메시지");
        when(user.getPreferredLanguage()).thenReturn("unsupported");
        assertThat(NotificationMessages.format(user, template, "닉네임")).isEqualTo("닉네임님의 메시지");

        when(user.getPreferredLanguage()).thenReturn("en-us");
        assertThat(NotificationMessages.format(user, "미등록 %s 안내", "50% 행사"))
            .isEqualTo("미등록 50% 행사 안내");
        assertThat(NotificationMessages.translateKnown(user, "미등록 안내")).isEqualTo("미등록 안내");
        assertThat(NotificationMessages.translateKnown(user, template)).isEqualTo(template);
        assertThat(NotificationMessages.translateKnown(user, null)).isNull();
        assertThat(NotificationMessages.format(user, "%s에 모집 정보가 새로 업데이트되었어요.\n행사 내용 둘러보기", "KOIN"))
            .isEqualTo("KOIN has new recruitment information.\nView event details");
    }

    @Test
    void 시스템문구와_같은_사용자_제목과_쪽지도_원문을_보존한다() {
        User user = mock(User.class);
        when(user.getPreferredLanguage()).thenReturn("en-US");
        String title = "지원하신 X 모집이 마감되어";
        assertThat(NotificationMessages.teamMessage(user, TeamRecruitmentNotificationType.APPLICATION_REJECTED,
            title, title + " 지원이 거절되었어요."))
            .isEqualTo("Your application to " + title + " was declined.");
        var chat = new NotificationFactory().generateChatMessageNotification(MobileAppPath.SHOP,
            1, 2, "닉네임", "주문이 접수되었어요", user);
        assertThat(chat.getTitle()).isEqualTo("Message from 닉네임");
        assertThat(chat.getMessage()).isEqualTo("주문이 접수되었어요");
    }
}
