package in.koreatech.koin.domain.bus.exception;

public class BusIllegalStation extends IllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "버스 정류장이 잘못되었습니다.";

    public BusIllegalStation(String message) {
        super(message);
    }

    public static BusIllegalStation withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new BusIllegalStation(message);
    }
}
