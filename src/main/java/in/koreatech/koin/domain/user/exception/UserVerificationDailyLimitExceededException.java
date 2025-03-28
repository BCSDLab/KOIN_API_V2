package in.koreatech.koin.domain.user.exception;

import in.koreatech.koin._common.exception.custom.KoinException;

public class UserVerificationDailyLimitExceededException extends KoinException {

    private static final String DEFAULT_MESSAGE = "하루 인증 최대 횟수 초과";

    protected UserVerificationDailyLimitExceededException(String message) {
        super(message);
    }

    protected UserVerificationDailyLimitExceededException(String message, String detail) {
        super(message, detail);
    }

    public static UserVerificationDailyLimitExceededException withDetail(String detail) {
        return new UserVerificationDailyLimitExceededException(DEFAULT_MESSAGE, detail);
    }
} 