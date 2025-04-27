package in.koreatech.koin.domain.user.exception;

import in.koreatech.koin._common.exception.custom.DuplicationException;

public class DuplicationLoginIdException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 존재하는 아이디입니다.";

    public DuplicationLoginIdException(String message) {
        super(message);
    }

    public DuplicationLoginIdException(String message, String detail) {
        super(message, detail);
    }

    public static DuplicationLoginIdException withDetail(String detail) {
        return new DuplicationLoginIdException(DEFAULT_MESSAGE, detail);
    }
}
