package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class BusNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "버스가 존재하지 않습니다.";

    public BusNotFoundException(String message) {
        super(message);
    }

    public BusNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static BusNotFoundException withDetail(String detail) {
        return new BusNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
