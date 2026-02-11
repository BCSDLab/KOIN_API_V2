package in.koreatech.koin.domain.callvan.event;

public record CallvanNewMessageEvent(
    Integer postId,
    String senderNickname,
    Integer sendUserId,
    String content
) {

}

