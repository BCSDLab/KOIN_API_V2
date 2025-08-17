package in.koreatech.koin.domain.land.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class LandNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "복덕방이 존재하지 않습니다.";

    public LandNotFoundException(String message) {
        super(message);
    }

    public LandNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static LandNotFoundException withDetail(String detail) {
        return new LandNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
