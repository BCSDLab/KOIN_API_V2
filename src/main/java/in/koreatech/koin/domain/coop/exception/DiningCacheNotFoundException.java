package in.koreatech.koin.domain.coop.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class DiningCacheNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "식단 캐시가 존재하지 않습니다.";

    public DiningCacheNotFoundException(String message) {
        super(message);
    }

    public DiningCacheNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static DiningCacheNotFoundException withDetail(String detail) {
        return new DiningCacheNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
