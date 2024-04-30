package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.KoinIllegalArgumentException;

public class BusIllegalArrivalTime extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "버스 도착 시각이 잘못되었습니다.";

    public BusIllegalArrivalTime(String message) {
        super(message);
    }

    public BusIllegalArrivalTime(String message, String detail) {
        super(message, detail);
    }

    public static BusIllegalArrivalTime withDetail(String detail) {
        return new BusIllegalArrivalTime(DEFAULT_MESSAGE, detail);
    }
}
