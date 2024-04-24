package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class BusTypeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "버스 타입이 존재하지 않습니다.";

    public BusTypeNotFoundException(String message) {
        super(message);
    }

    public static BusTypeNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new BusTypeNotFoundException(message);
    }

    @Override
    public String getDefaultMessage() {
        return DEFAULT_MESSAGE;
    }
}
