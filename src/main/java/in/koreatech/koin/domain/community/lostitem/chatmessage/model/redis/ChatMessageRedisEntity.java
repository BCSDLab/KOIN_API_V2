package in.koreatech.koin.domain.community.lostitem.chatmessage.model.redis;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "ChatMessage")
public class ChatMessageRedisEntity {

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

    public static ChatMessageRedisEntity toRedisEntity(ChatMessageEntity message) {
        return ChatMessageRedisEntity.builder()
            .id(message.getId())
            .articleId(message.getArticleId())
            .chatRoomId(message.getChatRoomId())
            .userId(message.getUserId())
            .isRead(message.getIsRead())
            .isDeleted(message.getIsDeleted())
            .isImage(message.getIsImage())
            .nickName(message.getNickName())
            .contents(message.getContents())
            .createdAt(message.getCreatedAt())
            .build();
    }
}
