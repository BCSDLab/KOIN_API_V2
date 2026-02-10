package in.koreatech.koin.domain.callvan.model;

import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.callvan.model.enums.CallvanMessageType;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "callvan_chat_message")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class CallvanChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "callvan_chat_room_id", nullable = false)
    private CallvanChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "sender_nickname", nullable = false, length = 50)
    private String senderNickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private CallvanMessageType messageType = CallvanMessageType.TEXT;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_image", nullable = false)
    private Boolean isImage = false;

    @Column(name = "is_left_user", nullable = false)
    private Boolean isLeftUser = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private CallvanChatMessage(
            CallvanChatRoom chatRoom,
            User sender,
            String senderNickname,
            CallvanMessageType messageType,
            String content,
            Boolean isImage,
            Boolean isLeftUser,
            Boolean isDeleted) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.senderNickname = senderNickname;
        this.messageType = messageType != null ? messageType : CallvanMessageType.TEXT;
        this.content = content;
        this.isImage = isImage != null ? isImage : false;
        this.isLeftUser = isLeftUser != null ? isLeftUser : false;
        this.isDeleted = isDeleted != null ? isDeleted : false;
    }
}
