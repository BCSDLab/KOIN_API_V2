package in.koreatech.koin.domain.user.exception;

public class UserNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 사용자입니다.";

    public UserNotFoundException() {
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException witDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new UserNotFoundException(message);
    }
}
