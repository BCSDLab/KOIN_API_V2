package in.koreatech.koin.global.auth.exception;

public class AuthenticationException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "올바르지 않은 인증정보입니다.";
    private final String detail;

    public AuthenticationException(String message) {
        super(message);
        this.detail = null;
    }

    public AuthenticationException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static AuthenticationException withDetail(String detail) {
        return new AuthenticationException(DEFAULT_MESSAGE, detail);
    }

    public String getDetail() {
        return detail;
    }
}
