package in.koreatech.koin.domain.community.exception;

import in.koreatech.koin.domain.owner.exception.VerificationDailyLimitExceededException;
import in.koreatech.koin.global.exception.KoinException;

public class KeywordLimitExceededException extends KoinException {
    private static final String DEFAULT_MESSAGE = "키워드 최대 개수 초과";

    protected KeywordLimitExceededException(String message) {
        super(message);
    }

    protected KeywordLimitExceededException(String message, String detail) {
        super(message, detail);
    }

    public static KeywordLimitExceededException withDetail(String detail) {
        return new KeywordLimitExceededException(DEFAULT_MESSAGE, detail);
    }
}
