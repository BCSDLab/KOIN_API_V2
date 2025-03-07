package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class BusIllegalStationException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "버스 정류장이 잘못되었습니다.";

    public BusIllegalStationException(String message) {
        super(message);
    }

    public BusIllegalStationException(String message, String detail) {
        super(message, detail);
    }

    public static BusIllegalStationException withDetail(String detail) {
        return new BusIllegalStationException(DEFAULT_MESSAGE, detail);
    }
}
