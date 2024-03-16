package in.koreatech.koin.domain.bus.exception;

public class BusIllegalStationException extends IllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "버스 정류장이 잘못되었습니다.";

    public BusIllegalStationException(String message) {
        super(message);
    }

    public static BusIllegalStationException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new BusIllegalStationException(message);
    }
}
