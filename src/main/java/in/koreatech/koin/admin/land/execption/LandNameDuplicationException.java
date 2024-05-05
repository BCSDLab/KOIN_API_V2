package in.koreatech.koin.admin.land.execption;

import in.koreatech.koin.global.exception.DuplicationException;

public class LandNameDuplicationException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "매물 이름은 사용중입니다";

    public LandNameDuplicationException(String message) {
        super(message);
    }

    public LandNameDuplicationException(String message, String detail) {
        super(message, detail);
    }

    public static LandNameDuplicationException withDetail(String detail) {
        return new LandNameDuplicationException(DEFAULT_MESSAGE, detail);
    }
}

