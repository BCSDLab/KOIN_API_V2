package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class AccessHistoryNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 접속 이력입니다.";

    public AccessHistoryNotFoundException(String message) {
        super(message);
    }

    public AccessHistoryNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static AccessHistoryNotFoundException withDetail(String detail) {
        return new AccessHistoryNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
