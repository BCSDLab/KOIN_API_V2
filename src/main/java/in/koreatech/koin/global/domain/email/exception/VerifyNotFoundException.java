package in.koreatech.koin.global.domain.email.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class VerifyNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 인증정보입니다.";

    public VerifyNotFoundException(String message) {
        super(message);
    }

    public static VerifyNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new VerifyNotFoundException(message);
    }
}
