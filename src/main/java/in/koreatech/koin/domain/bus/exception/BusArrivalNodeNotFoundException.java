package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class BusArrivalNodeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "버스 경로 상에 해당 노드가 존재하지 않습니다.";

    public BusArrivalNodeNotFoundException(String message) {
        super(message);
    }

    public BusArrivalNodeNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static BusArrivalNodeNotFoundException withDetail(String detail) {
        return new BusArrivalNodeNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
