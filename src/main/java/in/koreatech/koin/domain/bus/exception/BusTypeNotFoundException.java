package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class BusTypeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "버스 타입이 존재하지 않습니다.";
    private final String detail;

    public BusTypeNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public BusTypeNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static BusTypeNotFoundException withDetail(String detail) {
        return new BusTypeNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
