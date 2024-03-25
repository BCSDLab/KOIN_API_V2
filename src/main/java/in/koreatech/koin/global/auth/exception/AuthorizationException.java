package in.koreatech.koin.global.auth.exception;

public class AuthorizationException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "권한이 없습니다.";

    public AuthorizationException() {
    }

    public AuthorizationException(String message) {
        super(message);
    }

    public static AuthorizationException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new AuthorizationException(message);
    }
}
