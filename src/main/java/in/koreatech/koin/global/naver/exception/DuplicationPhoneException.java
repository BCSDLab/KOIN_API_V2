package in.koreatech.koin.global.naver.exception;

import in.koreatech.koin.global.exception.DuplicationException;

public class DuplicationPhoneException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "존재하는 휴대폰번호입니다.";

    public DuplicationPhoneException(String message) {
        super(message);
    }

    public static DuplicationPhoneException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new DuplicationPhoneException(message);
    }
}
