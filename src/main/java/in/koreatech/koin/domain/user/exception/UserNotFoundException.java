package in.koreatech.koin.domain.user.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class UserNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 사용자입니다.";
    private final String detail;

    public UserNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public UserNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static UserNotFoundException withDetail(String detail) {
        return new UserNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
