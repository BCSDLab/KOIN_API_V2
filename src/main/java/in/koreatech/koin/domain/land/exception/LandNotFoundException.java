package in.koreatech.koin.domain.land.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class LandNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "복덕방이 존재하지 않습니다.";
    private final String detail;

    public LandNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public LandNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static LandNotFoundException withDetail(String detail) {
        return new LandNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
