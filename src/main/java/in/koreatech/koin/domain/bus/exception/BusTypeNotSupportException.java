package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class BusTypeNotSupportException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "해당 버스타입에는 지원하지 않는 기능입니다.";

    public BusTypeNotSupportException(String message) {
        super(message);
    }

    public BusTypeNotSupportException(String message, String detail) {
        super(message, detail);
    }

    public static BusTypeNotSupportException withDetail(String detail) {
        return new BusTypeNotSupportException(DEFAULT_MESSAGE, detail);
    }
}
