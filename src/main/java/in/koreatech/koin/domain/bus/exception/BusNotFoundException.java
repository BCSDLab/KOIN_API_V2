package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class BusNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "버스가 존재하지 않습니다.";
    private final String detail;

    public BusNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public BusNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static BusNotFoundException withDetail(String detail) {
        return new BusNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
