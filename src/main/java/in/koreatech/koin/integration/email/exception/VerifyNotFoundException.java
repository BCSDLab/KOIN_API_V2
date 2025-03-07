package in.koreatech.koin.integration.email.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class VerifyNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 인증정보입니다.";

    public VerifyNotFoundException(String message) {
        super(message);
    }

    public VerifyNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static VerifyNotFoundException withDetail(String detail) {
        return new VerifyNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
