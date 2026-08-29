package in.koreatech.koin.domain.teamrecruitment.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "team_recruitment_chat_room_member")
@NoArgsConstructor(access = PROTECTED)
public class TeamRecruitmentChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private TeamRecruitmentChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 이 값보다 작은 message_id는 이미 읽은 것으로 처리
    @Column(name = "last_read_message_id")
    private Integer lastReadMessageId;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public void updateLastReadMessageId(Integer messageId) {
        if (this.lastReadMessageId == null || messageId > this.lastReadMessageId) {
            this.lastReadMessageId = messageId;
        }
    }

    @Builder
    private TeamRecruitmentChatRoomMember(TeamRecruitmentChatRoom chatRoom, User user, Integer lastReadMessageId) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.lastReadMessageId = lastReadMessageId;
        this.joinedAt = LocalDateTime.now();
    }
}
