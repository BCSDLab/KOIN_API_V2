package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class BusTypeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "버스 타입이 존재하지 않습니다.";

    public BusTypeNotFoundException(String message) {
        super(message);
    }

    public BusTypeNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static BusTypeNotFoundException withDetail(String detail) {
        return new BusTypeNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
