package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class BusCacheNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "버스 캐시가 존재하지 않습니다.";

    public BusCacheNotFoundException(String message) {
        super(message);
    }

    public BusCacheNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static BusCacheNotFoundException withDetail(String detail) {
        return new BusCacheNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
