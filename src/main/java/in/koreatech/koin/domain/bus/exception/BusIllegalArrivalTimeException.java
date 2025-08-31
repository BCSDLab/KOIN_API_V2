package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class BusIllegalArrivalTimeException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "버스 도착 시각이 잘못되었습니다.";

    public BusIllegalArrivalTimeException(String message) {
        super(message);
    }

    public BusIllegalArrivalTimeException(String message, String detail) {
        super(message, detail);
    }

    public static BusIllegalArrivalTimeException withDetail(String detail) {
        return new BusIllegalArrivalTimeException(DEFAULT_MESSAGE, detail);
    }
}
