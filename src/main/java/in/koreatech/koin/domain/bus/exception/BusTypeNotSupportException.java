package in.koreatech.koin.domain.bus.exception;

public class BusTypeNotSupportException extends IllegalArgumentException {
    private static final String DEFAULT_MESSAGE = "해당 버스타입에는 지원하지 않는 기능입니다.";

    public BusTypeNotSupportException(String message) {
        super(message);
    }

    public static BusTypeNotSupportException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new BusTypeNotSupportException(message);
    }
}

