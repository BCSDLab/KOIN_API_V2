package in.koreatech.koin.domain.land.exception;

public class LandNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "복덕방이 존재하지 않습니다.";

    public LandNotFoundException(String message) {
        super(message);
    }

    public static LandNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new LandNotFoundException(message);
    }
}
