package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class BusArrivalNodeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "버스 경로 상에 해당 노드가 존재하지 않습니다.";
    private final String detail;

    public BusArrivalNodeNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public BusArrivalNodeNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static BusArrivalNodeNotFoundException withDetail(String detail) {
        return new BusArrivalNodeNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
