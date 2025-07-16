package in.koreatech.koin.domain.community.lostitem.chatroom.model;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Field;

import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LostItemChatMessageEntity {

    @Field("tsid")
    private String tsid;

    @Field("user_id")
    private Integer userId;

    @Field("is_read")
    private Boolean isRead;

    @Field("is_deleted")
    private Boolean isDeleted;

    @Field("nickname")
    private String nickName;

    @Field("contents")
    private String contents;

    @Field("created_at")
    private LocalDateTime createdAt;

    public static LostItemChatMessageEntity from(ChatMessageEntity domainMessage) {
        return LostItemChatMessageEntity.builder()
            .tsid(domainMessage.getId().toString())
            .userId(domainMessage.getUserId())
            .isRead(domainMessage.getIsRead())
            .isDeleted(domainMessage.getIsDeleted())
            .nickName(domainMessage.getNickName())
            .contents(domainMessage.getContents())
            .createdAt(domainMessage.getCreatedAt())
            .build();
    }
}
