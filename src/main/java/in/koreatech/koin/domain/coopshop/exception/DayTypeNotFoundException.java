package in.koreatech.koin.domain.coopshop.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class DayTypeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 타입의 요일이 존재하지 않습니다.";

    public DayTypeNotFoundException(String message) {
        super(message);
    }

    public DayTypeNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static DayTypeNotFoundException withDetail(String detail) {
        return new DayTypeNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
