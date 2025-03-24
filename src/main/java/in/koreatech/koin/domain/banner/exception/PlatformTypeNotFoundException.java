package in.koreatech.koin.domain.banner.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class PlatformTypeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 플랫폼타입이 존재하지 않습니다.";

    public PlatformTypeNotFoundException(String message) {
        super(message);
    }

    public PlatformTypeNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static PlatformTypeNotFoundException withDetail(String detail) {
        return new PlatformTypeNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
