package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin._common.exception.custom.DuplicationException;

public class AbtestAlreadyExistException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 존재하는 AB테스트입니다.";

    public AbtestAlreadyExistException(String message) {
        super(message);
    }

    public AbtestAlreadyExistException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestAlreadyExistException withDetail(String detail) {
        return new AbtestAlreadyExistException(DEFAULT_MESSAGE, detail);
    }
}
