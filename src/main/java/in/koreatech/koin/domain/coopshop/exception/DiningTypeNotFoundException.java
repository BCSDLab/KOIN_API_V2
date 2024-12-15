package in.koreatech.koin.domain.coopshop.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class DiningTypeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당하는 식단 타입이 존재하지 않습니다.";

    public DiningTypeNotFoundException(String message) {
        super(message);
    }

    public DiningTypeNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static DiningTypeNotFoundException withDetail(String detail) {
        return new DiningTypeNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
