package in.koreatech.koin.domain.coop.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class CoopNotFoundException extends DataNotFoundException {
    private static final String DEFAULT_MESSAGE = "영양사님 계정이 존재하지 않습니다.";

    public CoopNotFoundException(String message) {
        super(message);
    }

    public CoopNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static DiningCacheNotFoundException withDetail(String detail) {
        return new DiningCacheNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
