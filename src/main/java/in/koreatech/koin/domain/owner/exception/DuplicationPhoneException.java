package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.global.exception.DuplicationException;

public class DuplicationPhoneException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 존재하는 휴대폰번호입니다.";

    public DuplicationPhoneException(String message) {
        super(message);
    }

    public DuplicationPhoneException(String message, String detail) {
        super(message, detail);
    }

    public static DuplicationPhoneException withDetail(String detail) {
        return new DuplicationPhoneException(DEFAULT_MESSAGE, detail);
    }
}
