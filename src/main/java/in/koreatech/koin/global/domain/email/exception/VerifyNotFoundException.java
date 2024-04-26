package in.koreatech.koin.global.domain.email.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class VerifyNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 인증정보입니다.";

    private final String detail;

    public VerifyNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public VerifyNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static VerifyNotFoundException withDetail(String detail) {
        return new VerifyNotFoundException(DEFAULT_MESSAGE, detail);
    }

    public String getDetail() {
        return detail;
    }
}
