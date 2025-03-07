package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class BusStationNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "버스 정류장이 존재하지 않습니다.";

    public BusStationNotFoundException(String message) {
        super(message);
    }

    public BusStationNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static BusStationNotFoundException withDetail(String detail) {
        return new BusStationNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
