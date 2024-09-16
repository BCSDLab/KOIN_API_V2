package in.koreatech.koin.domain.mobileversion.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class MobileVersionTypeNotFoundException extends DataNotFoundException {

    public static final String DEFAULT_MESSAGE = "존재하지 않는 버전 타입입니다.";

    public MobileVersionTypeNotFoundException(String message) {
        super(message);
    }

    public MobileVersionTypeNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static MobileVersionTypeNotFoundException withDetail(String detail) {
        return new MobileVersionTypeNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
