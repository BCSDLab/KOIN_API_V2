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
