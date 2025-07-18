package in.koreatech.koin.domain.community.lostitem.chatmessage.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "ChatMessage")
public class ChatMessageEntity {

    @Id
    private Long id;

    @Indexed
    private Integer articleId;

    @Indexed
    private Integer chatRoomId;

    private Integer userId;

    private Boolean isRead;

    private Boolean isDeleted;

    private Boolean isImage;

    private String nickName;

    private String contents;

    private LocalDateTime createdAt;

    public void readMessage() {
        this.isRead = true;
    }

    public static ChatMessageEntity create(
        Integer articleId, Integer chatRoomId, Integer userId, Boolean isRead, ChatMessage message, Long id
    ) {
        return ChatMessageEntity.builder()
            .id(id)
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
