package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class BusCacheNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "버스 캐시가 존재하지 않습니다.";

    public BusCacheNotFoundException(String message) {
        super(message);
    }

    public static BusCacheNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new BusCacheNotFoundException(message);
    }
}
