package in.koreatech.koin.unit.domain.team.recruitment.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;

class TeamRecruitmentCommunicationModelTest {

    @Test
    void 마지막으로_읽은_메시지는_앞으로만_이동한다() {
        TeamRecruitmentChatMember member = TeamRecruitmentChatMember.builder()
            .lastReadMessageId(10)
            .build();

        member.advanceLastReadMessageId(9);
        member.advanceLastReadMessageId(11);

        assertThat(member.getLastReadMessageId()).isEqualTo(11);
    }

    @Test
    void 이미지_여부를_생략하면_텍스트_메시지로_생성한다() {
        TeamRecruitmentChatMessage message = TeamRecruitmentChatMessage.builder()
            .senderNickname("사용자")
            .content("안녕하세요")
            .build();

        assertThat(message.getIsImage()).isFalse();
    }

    @Test
    void 알림은_최초_읽음_시각을_유지하고_삭제할_수_있다() {
        LocalDateTime firstReadAt = LocalDateTime.of(2026, 8, 28, 10, 0);
        TeamRecruitmentNotification notification = TeamRecruitmentNotification.builder().build();

        notification.markAsRead(firstReadAt);
        notification.markAsRead(firstReadAt.plusMinutes(1));
        notification.delete();

        assertThat(notification.getReadAt()).isEqualTo(firstReadAt);
        assertThat(notification.getIsDeleted()).isTrue();
    }
}
