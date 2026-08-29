package in.koreatech.koin.domain.team.recruitment.model;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
    name = "team_recruitment_chat_member",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_team_recruitment_chat_member_room_user",
        columnNames = {"chat_room_id", "user_id"}
    )
)
@NoArgsConstructor(access = PROTECTED)
public class TeamRecruitmentChatMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false, updatable = false)
    private TeamRecruitmentChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(name = "last_read_message_id")
    private Integer lastReadMessageId;

    @Builder
    private TeamRecruitmentChatMember(
        Integer id,
        TeamRecruitmentChatRoom chatRoom,
        User user,
        Integer lastReadMessageId
    ) {
        this.id = id;
        this.chatRoom = chatRoom;
        this.user = user;
        this.lastReadMessageId = lastReadMessageId;
    }

    void assignChatRoom(TeamRecruitmentChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public void advanceLastReadMessageId(Integer messageId) {
        if (messageId != null && (lastReadMessageId == null || messageId > lastReadMessageId)) {
            lastReadMessageId = messageId;
        }
    }
}
