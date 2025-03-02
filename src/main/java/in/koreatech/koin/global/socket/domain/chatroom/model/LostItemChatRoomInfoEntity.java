package in.koreatech.koin.global.socket.domain.chatroom.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@Document(collection = "lost_item_chat_room")
public class LostItemChatRoomInfoEntity {

    @Id
    @Field("_id")
    private ObjectId id;

    @Field("article_id")
    private Integer articleId;

    @Field("chatroom_id")
    private Integer chatRoomId;

    // 글을 올린 사용자 ID
    @Field("article_author_id")
    private Integer authorId;

    // 글을 보고 쪽지를 보낸 사용자 ID
    @Field("item_owner_id")
    private Integer ownerId;

    @Field("messages")
    private List<LostItemChatMessageEntity> messageList;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Builder
    public LostItemChatRoomInfoEntity(Integer articleId, Integer chatRoomId, Integer authorId, Integer ownerId,
        LocalDateTime createdAt) {
        this.articleId = articleId;
        this.chatRoomId = chatRoomId;
        this.authorId = authorId;
        this.ownerId = ownerId;
        this.messageList = new ArrayList<>();
        this.createdAt = createdAt;
    }

    public void addMessage(LostItemChatMessageEntity message) {
        this.messageList.add(message);
    }
}
