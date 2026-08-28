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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "team_recruitment_chat_message")
@NoArgsConstructor(access = PROTECTED)
public class TeamRecruitmentChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false, updatable = false)
    private TeamRecruitmentChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false, updatable = false)
    private User sender;

    @NotBlank
    @Size(max = 50)
    @Column(name = "sender_nickname", nullable = false, length = 50, updatable = false)
    private String senderNickname;

    @NotBlank
    @Column(name = "content", nullable = false, columnDefinition = "TEXT", updatable = false)
    private String content;

    @NotNull
    @Column(name = "is_image", nullable = false, updatable = false)
    private Boolean isImage;

    @Builder
    private TeamRecruitmentChatMessage(
        Integer id,
        TeamRecruitmentChatRoom chatRoom,
        User sender,
        String senderNickname,
        String content,
        Boolean isImage
    ) {
        this.id = id;
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.senderNickname = senderNickname;
        this.content = content;
        this.isImage = isImage != null ? isImage : false;
    }
}
