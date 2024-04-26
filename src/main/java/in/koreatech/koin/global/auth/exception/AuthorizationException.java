package in.koreatech.koin.global.auth.exception;

public class AuthorizationException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "권한이 없습니다.";
    private final String detail;

    public AuthorizationException(String message) {
        super(message);
        this.detail = null;
    }

    public AuthorizationException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static AuthorizationException withDetail(String detail) {
        return new AuthorizationException(DEFAULT_MESSAGE, detail);
    }

    public String getDetail() {
        return detail;
    }
}
