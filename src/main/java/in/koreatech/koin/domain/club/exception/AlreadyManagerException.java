package in.koreatech.koin.domain.club.exception;

import in.koreatech.koin._common.exception.custom.KoinException;

public class AlreadyManagerException extends KoinException {
    private static final String DEFAULT_MESSAGE = "이미 동아리의 관리자입니다.";

    public AlreadyManagerException(String message) {
        super(message);
    }

    public AlreadyManagerException(String message, String detail) {
        super(message, detail);
    }

    public static AlreadyManagerException withDetail(String detail) {
        return new AlreadyManagerException(DEFAULT_MESSAGE, detail);
    }
}
