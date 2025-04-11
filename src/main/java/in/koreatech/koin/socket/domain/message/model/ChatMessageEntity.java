package in.koreatech.koin.socket.domain.message.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageEntity {

    private Long id;

    private Integer articleId;

    private Integer chatRoomId;

    private Integer userId;

    private Boolean isRead;

    private Boolean isDeleted;

    private String nickName;

    private String contents;

    private Boolean isImage;

    private LocalDateTime createdAt;

    public void addMessageId(Long id) {
        this.id = id;
    }

    public static ChatMessageEntity create(Integer articleId, Integer chatRoomId, Integer userId, Boolean isRead, ChatMessageCommand message) {
        return ChatMessageEntity.builder()
            .articleId(articleId)
            .chatRoomId(chatRoomId)
            .userId(userId)
            .isRead(isRead)
            .isDeleted(false)
            .isImage(message.isImage())
            .nickName(message.userNickname())
            .contents(message.content())
            .createdAt(LocalDateTime.now())
            .build();
    }
}
