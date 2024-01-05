package in.koreatech.koin.domain.auth.exception;

public class AuthException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "올바르지 않은 인증정보입니다.";

    public AuthException() {
    }

    public AuthException(String message) {
        super(message);
    }

    public static AuthException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new AuthException(message);
    }
}
