package in.koreatech.koin.socket.domain.chatroom.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;

public class SelfChatNotAllowedException extends KoinIllegalStateException {

    private static final String DEFAULT_MESSAGE = "사용자가 자신과 채팅방을 생성할 수 없습니다.";

    public SelfChatNotAllowedException(String message) {
        super(message);
    }

    public SelfChatNotAllowedException(String message, String detail) {
        super(message, detail);
    }

    public static SelfChatNotAllowedException withDetail(String detail) {
        return new SelfChatNotAllowedException(DEFAULT_MESSAGE, detail);
    }
}
