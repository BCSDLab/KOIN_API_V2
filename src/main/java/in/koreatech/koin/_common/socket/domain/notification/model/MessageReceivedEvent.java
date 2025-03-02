package in.koreatech.koin._common.socket.domain.notification.model;

import in.koreatech.koin._common.socket.domain.message.model.ChatMessageCommand;

public record MessageReceivedEvent(
    Integer articleId,
    Integer chatRoomId,
    Integer userId,
    ChatMessageCommand messageCommand
) {
}
