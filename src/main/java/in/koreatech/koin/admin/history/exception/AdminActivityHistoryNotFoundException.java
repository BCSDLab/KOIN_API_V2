package in.koreatech.koin.admin.history.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class AdminActivityHistoryNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 히스토리 입니다.";

    public AdminActivityHistoryNotFoundException(String message) {
        super(message);
    }

    public AdminActivityHistoryNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static AdminActivityHistoryNotFoundException withDetail(String detail) {
        return new AdminActivityHistoryNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
