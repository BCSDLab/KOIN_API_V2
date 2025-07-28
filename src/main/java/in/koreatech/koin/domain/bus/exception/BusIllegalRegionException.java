package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class BusIllegalRegionException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "버스 지역 구분이 잘못되었습니다.";

    public BusIllegalRegionException(String message) {
        super(message);
    }

    public BusIllegalRegionException(String message, String detail) {
        super(message, detail);
    }

    public static BusIllegalStationException withDetail(String detail) {
        return new BusIllegalStationException(DEFAULT_MESSAGE, detail);
    }
}
