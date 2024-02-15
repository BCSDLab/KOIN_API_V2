package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class BusStationNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "버스 정류장이 존재하지 않습니다.";

    public BusStationNotFoundException(String message) {
        super(message);
    }

    public static BusStationNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new BusStationNotFoundException(message);
    }
}
