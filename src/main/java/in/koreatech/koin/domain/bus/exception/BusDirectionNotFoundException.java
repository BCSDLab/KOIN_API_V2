package in.koreatech.koin.domain.bus.exception;

public class BusDirectionNotFoundException extends IllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "정상적인 버스 방향이 아닙니다.";

    public BusDirectionNotFoundException(String message) {
        super(message);
    }

    public static BusDirectionNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new BusDirectionNotFoundException(message);
    }
}
