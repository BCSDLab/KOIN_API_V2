package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin.global.exception.KoinIllegalArgumentException;

public class AbtestNotInProgressUserException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "진행중이지 않은 AB테스트입니다.";

    public AbtestNotInProgressUserException(String message) {
        super(message);
    }

    public AbtestNotInProgressUserException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestNotInProgressUserException withDetail(String detail) {
        return new AbtestNotInProgressUserException(DEFAULT_MESSAGE, detail);
    }
}
