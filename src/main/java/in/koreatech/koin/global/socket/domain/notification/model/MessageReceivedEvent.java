package in.koreatech.koin.global.socket.domain.notification.model;

import in.koreatech.koin.global.socket.domain.message.model.ChatMessageCommand;

public record MessageReceivedEvent(
    Integer articleId,
    Integer chatRoomId,
    Integer userId,
    ChatMessageCommand messageCommand
) {
}
