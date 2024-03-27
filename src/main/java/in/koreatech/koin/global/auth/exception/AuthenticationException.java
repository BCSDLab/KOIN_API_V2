package in.koreatech.koin.global.auth.exception;

public class AuthenticationException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "올바르지 않은 인증정보입니다.";

    public AuthenticationException() {
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public static AuthenticationException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new AuthenticationException(message);
    }
}
