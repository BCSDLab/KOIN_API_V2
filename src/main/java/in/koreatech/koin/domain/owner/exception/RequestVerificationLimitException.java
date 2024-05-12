package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.global.exception.KoinException;

public class RequestVerificationLimitException extends KoinException {

    private static final String DEFAULT_MESSAGE = "하루 인증 최대 횟수 초과";

    protected RequestVerificationLimitException(String message) {
        super(message);
    }

    protected RequestVerificationLimitException(String message, String detail) {
        super(message, detail);
    }

    public static RequestVerificationLimitException withDetail(String detail) {
        return new RequestVerificationLimitException(DEFAULT_MESSAGE, detail);
    }
}
