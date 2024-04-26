package in.koreatech.koin.global.domain.email.exception;

import in.koreatech.koin.global.exception.DuplicationException;

public class DuplicationEmailException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "존재하는 이메일입니다.";
    private final String detail;

    public DuplicationEmailException(String message) {
        super(message);
        this.detail = null;
    }

    public DuplicationEmailException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static DuplicationEmailException withDetail(String detail) {
        return new DuplicationEmailException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
