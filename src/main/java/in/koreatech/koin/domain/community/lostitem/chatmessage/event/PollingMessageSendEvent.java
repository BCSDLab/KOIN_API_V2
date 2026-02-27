package in.koreatech.koin.domain.community.lostitem.chatmessage.event;

import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessage;

public record PollingMessageSendEvent(
    Integer articleId,
    Integer chatRoomId,
    Integer userId,
    Integer receiverId,
    ChatMessage chatMessage
) {
}
