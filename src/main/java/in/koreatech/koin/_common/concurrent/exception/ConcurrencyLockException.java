package in.koreatech.koin._common.concurrent.exception;

import in.koreatech.koin._common.exception.custom.KoinException;

public class ConcurrencyLockException extends KoinException {

    private static final String DEFAULT_MESSAGE = "현재 요청을 처리할 수 없습니다. 잠시 후 다시 시도해 주세요.";

    public ConcurrencyLockException(String message) {
        super(message);
    }

    public ConcurrencyLockException(String message, String detail) {
        super(message, detail);
    }

    public static ConcurrencyLockException withDetail(String detail) {
        return new ConcurrencyLockException(DEFAULT_MESSAGE, detail);
    }
}
