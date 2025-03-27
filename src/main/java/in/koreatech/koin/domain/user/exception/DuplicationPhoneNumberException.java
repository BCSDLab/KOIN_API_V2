package in.koreatech.koin.domain.user.exception;

import in.koreatech.koin._common.exception.custom.DuplicationException;

public class DuplicationPhoneNumberException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 존재하는 전화번호입니다.";

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
