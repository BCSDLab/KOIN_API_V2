package in.koreatech.koin.domain.community.keyword.exception;

import in.koreatech.koin.global.exception.custom.KoinException;

public class KeywordLimitExceededException extends KoinException {

    private static final String DEFAULT_MESSAGE = "저장할 수 있는 최대 키워드 수를 초과 했습니다.";

    public KeywordLimitExceededException(String message) {
        super(message);
    }

    public KeywordLimitExceededException(String message, String detail) {
        super(message, detail);
    }

    public static KeywordLimitExceededException withDetail(String detail) {
        return new KeywordLimitExceededException(DEFAULT_MESSAGE, detail);
    }
}
