package in.koreatech.koin.domain.community.lostitem.chatmessage.event;

import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessage;

public record MessageSendEvent(
    Integer articleId,
    Integer chatRoomId,
    Integer userId,
    ChatMessage chatMessage
) {

    public static MessageSendEvent from(
        Integer articleId,
        Integer chatRoomId,
        Integer userId,
        ChatMessage chatMessage
    ) {
        return new MessageSendEvent(articleId, chatRoomId, userId, chatMessage);
    }
}
