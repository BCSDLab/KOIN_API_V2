package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class BusArrivalNodeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "버스 경로 상에 해당 노드가 존재하지 않습니다.";

    public BusArrivalNodeNotFoundException(String message) {
        super(message);
    }

    public static BusArrivalNodeNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new BusArrivalNodeNotFoundException(message);
    }

    @Override
    public String getDefaultMessage() {
        return DEFAULT_MESSAGE;
    }
}
