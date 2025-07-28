package in.koreatech.koin.domain.dining.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class ExcelDiningPositionNotFoundException extends DataNotFoundException {
    private static final String DEFAULT_MESSAGE = "알맞는 ExcelDiningPosition이 존재하지 않습니다.";

    public ExcelDiningPositionNotFoundException(String message) {
        super(message);
    }

    public ExcelDiningPositionNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ExcelDiningPositionNotFoundException withDetail(String detail) {
        return new ExcelDiningPositionNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
