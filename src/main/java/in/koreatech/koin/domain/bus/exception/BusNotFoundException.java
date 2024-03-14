package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class BusNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "버스가 존재하지 않습니다.";

    public BusNotFoundException(String message) {
        super(message);
    }

    public static BusNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new BusNotFoundException(message);
    }
}
