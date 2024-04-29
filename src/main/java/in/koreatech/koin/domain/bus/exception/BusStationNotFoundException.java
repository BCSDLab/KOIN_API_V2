package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class BusStationNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "버스 정류장이 존재하지 않습니다.";
    private final String detail;

    public BusStationNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public BusStationNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static BusStationNotFoundException withDetail(String detail) {
        return new BusStationNotFoundException(DEFAULT_MESSAGE + detail, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
