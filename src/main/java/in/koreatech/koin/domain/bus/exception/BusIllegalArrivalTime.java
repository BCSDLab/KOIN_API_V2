package in.koreatech.koin.domain.bus.exception;

public class BusIllegalArrivalTime extends IllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "버스 도착 시각이 잘못되었습니다.";

    public BusIllegalArrivalTime(String message) {
        super(message);
    }

    public static BusIllegalArrivalTime withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new BusIllegalArrivalTime(message);
    }
}
