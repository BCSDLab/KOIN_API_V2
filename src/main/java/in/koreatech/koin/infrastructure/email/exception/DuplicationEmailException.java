package in.koreatech.koin.infrastructure.email.exception;

import in.koreatech.koin.global.exception.custom.DuplicationException;

public class DuplicationEmailException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 존재하는 이메일입니다.";

    public DuplicationEmailException(String message) {
        super(message);
    }

    public DuplicationEmailException(String message, String detail) {
        super(message, detail);
    }

    public static DuplicationEmailException withDetail(String detail) {
        return new DuplicationEmailException(DEFAULT_MESSAGE, detail);
    }
}
