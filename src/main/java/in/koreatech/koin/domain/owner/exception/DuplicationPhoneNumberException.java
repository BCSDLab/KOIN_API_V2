package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.global.exception.custom.DuplicationException;

public class DuplicationPhoneNumberException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 존재하는 휴대폰번호입니다.";

    public DuplicationPhoneNumberException(String message) {
        super(message);
    }

    public DuplicationPhoneNumberException(String message, String detail) {
        super(message, detail);
    }

    public static DuplicationPhoneNumberException withDetail(String detail) {
        return new DuplicationPhoneNumberException(DEFAULT_MESSAGE, detail);
    }
}
