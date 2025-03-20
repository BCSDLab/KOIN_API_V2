package in.koreatech.koin.admin.user.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class AdminNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 어드민 입니다.";

    public AdminNotFoundException(String message) {
        super(message);
    }

    public AdminNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static AdminNotFoundException withDetail(String detail) {
        return new AdminNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
