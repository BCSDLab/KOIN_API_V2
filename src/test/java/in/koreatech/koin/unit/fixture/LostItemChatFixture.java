package in.koreatech.koin.unit.fixture;

import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;

public class LostItemChatFixture {

    private LostItemChatFixture() {}

    public static LostItemChatRoomInfoEntity 분실물_게시글_채팅방(
        Integer articleId, Integer chatRoomId, Integer authorId, Integer messageSenderId) {

        return LostItemChatRoomInfoEntity.builder()
            .articleId(articleId)
            .chatRoomId(chatRoomId)
            .authorId(authorId)
            .ownerId(messageSenderId)
            .build();
    }
}
