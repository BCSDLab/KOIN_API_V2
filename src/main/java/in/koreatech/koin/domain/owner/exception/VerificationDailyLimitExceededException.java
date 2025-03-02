package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin._common.exception.custom.KoinException;

public class VerificationDailyLimitExceededException extends KoinException {

    private static final String DEFAULT_MESSAGE = "하루 인증 최대 횟수 초과";

    protected VerificationDailyLimitExceededException(String message) {
        super(message);
    }

    protected VerificationDailyLimitExceededException(String message, String detail) {
        super(message, detail);
    }

    public static VerificationDailyLimitExceededException withDetail(String detail) {
        return new VerificationDailyLimitExceededException(DEFAULT_MESSAGE, detail);
    }
}
