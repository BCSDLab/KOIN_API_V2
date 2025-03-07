package in.koreatech.koin.domain.community.keyword.exception;

import in.koreatech.koin._common.exception.custom.DuplicationException;

public class KeywordDuplicationException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "키워드는 중복될 수 없습니다.";

    public KeywordDuplicationException(String message) {
        super(message);
    }

    public KeywordDuplicationException(String message, String detail) {
        super(message, detail);
    }

    public static KeywordDuplicationException withDetail(String detail) {
        return new KeywordDuplicationException(DEFAULT_MESSAGE, detail);
    }
}
