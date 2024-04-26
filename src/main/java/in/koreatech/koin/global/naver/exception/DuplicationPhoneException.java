package in.koreatech.koin.global.naver.exception;

import in.koreatech.koin.global.exception.DuplicationException;

public class DuplicationPhoneException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "존재하는 휴대폰번호입니다.";
    private final String detail;

    public DuplicationPhoneException(String message) {
        super(message);
        this.detail = null;
    }

    public DuplicationPhoneException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static DuplicationPhoneException withDetail(String detail) {
        return new DuplicationPhoneException(DEFAULT_MESSAGE, detail);
    }

    public String getDetail() {
        return detail;
    }
}
