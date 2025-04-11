package in.koreatech.koin.socket.domain.chatroom.exception;

import in.koreatech.koin._common.auth.exception.AuthorizationException;

public class UserBlockException extends AuthorizationException {

    private static final String DEFAULT_MESSAGE = "차단된 사용자 입니다.";

    public UserBlockException(String message) {
        super(message);
    }

    public UserBlockException(String message, String detail) {
        super(message, detail);
    }

    public static UserBlockException withDetail(String detail) {
        return new UserBlockException(DEFAULT_MESSAGE, detail);
    }
}
