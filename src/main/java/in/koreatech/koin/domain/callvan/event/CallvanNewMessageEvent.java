package in.koreatech.koin.domain.callvan.event;

import in.koreatech.koin.domain.callvan.model.enums.CallvanMessageType;

public record CallvanNewMessageEvent(
    Integer postId,
    String senderNickname,
    Integer sendUserId,
    String content,
    CallvanMessageType messageType
) {

}

