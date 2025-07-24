package in.koreatech.koin.domain.community.lostitem.chatroom.model;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@Document(collection = "lost_item_chat_user_block")
public class LostItemChatUserBlockEntity {

    @Id
    @Field("_id")
    private ObjectId id;

    @Field("blocker_user_id")
    private Integer blockerUserId;

    @Field("blocked_user_id")
    private Integer blockedUserId;

    @Field("is_active")
    private Boolean isActive;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Builder
    public LostItemChatUserBlockEntity(Integer blockerUserId, Integer blockedUserId, Boolean isActive) {
        this.blockerUserId = blockerUserId;
        this.blockedUserId = blockedUserId;
        this.isActive = isActive;
        this.createdAt = LocalDateTime.now();
    }
}
