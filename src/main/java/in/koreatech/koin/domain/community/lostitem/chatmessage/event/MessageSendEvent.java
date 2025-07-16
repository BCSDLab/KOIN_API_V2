package in.koreatech.koin.domain.community.lostitem.chatmessage.event;

import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageCommand;

public record MessageSendEvent(
    Integer articleId,
    Integer chatRoomId,
    Integer userId,
    ChatMessageCommand messageCommand
) {

    public static MessageSendEvent from(
        Integer articleId,
        Integer chatRoomId,
        Integer userId,
        ChatMessageCommand messageCommand
    ) {
        return new MessageSendEvent(articleId, chatRoomId, userId, messageCommand);
    }
}
