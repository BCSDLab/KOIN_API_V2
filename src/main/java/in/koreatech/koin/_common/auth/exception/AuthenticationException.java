package in.koreatech.koin._common.auth.exception;

import in.koreatech.koin._common.exception.custom.KoinException;

public class AuthenticationException extends KoinException {

    private static final String DEFAULT_MESSAGE = "올바르지 않은 인증정보입니다.";

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, String detail) {
        super(message, detail);
    }

    public static AuthenticationException withDetail(String detail) {
        return new AuthenticationException(DEFAULT_MESSAGE, detail);
    }
}
