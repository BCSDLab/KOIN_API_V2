package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class BusDirectionNotFoundException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "정상적인 버스 방향이 아닙니다.";

    public BusDirectionNotFoundException(String message) {
        super(message);
    }

    public BusDirectionNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static BusDirectionNotFoundException withDetail(String detail) {
        return new BusDirectionNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
