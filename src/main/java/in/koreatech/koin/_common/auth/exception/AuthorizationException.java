package in.koreatech.koin._common.auth.exception;

import in.koreatech.koin._common.exception.custom.KoinException;

public class AuthorizationException extends KoinException {

    private static final String DEFAULT_MESSAGE = "권한이 없습니다.";

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, String detail) {
        super(message, detail);
    }

    public static AuthorizationException withDetail(String detail) {
        return new AuthorizationException(DEFAULT_MESSAGE, detail);
    }
}
