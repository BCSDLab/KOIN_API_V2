package in.koreatech.koin.domain.coop.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class DiningLimitDateException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "오늘 날짜 이후 기간은 설정할 수 없어요";

    public DiningLimitDateException(String message) {
        super(message);
    }

    public DiningLimitDateException(String message, String detail) {
        super(message, detail);
    }

    public static DiningLimitDateException withDetail(String detail) {
        return new DiningLimitDateException(DEFAULT_MESSAGE, detail);
    }
}
