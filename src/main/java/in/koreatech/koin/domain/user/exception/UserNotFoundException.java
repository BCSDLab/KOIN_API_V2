package in.koreatech.koin.domain.user.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class UserNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 사용자입니다.";

    public UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new UserNotFoundException(message);
    }
}
