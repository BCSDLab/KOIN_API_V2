package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class AbtestNotInProgressException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "진행중이지 않은 AB테스트입니다.";

    public AbtestNotInProgressException(String message) {
        super(message);
    }

    public AbtestNotInProgressException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestNotInProgressException withDetail(String detail) {
        return new AbtestNotInProgressException(DEFAULT_MESSAGE, detail);
    }
}
