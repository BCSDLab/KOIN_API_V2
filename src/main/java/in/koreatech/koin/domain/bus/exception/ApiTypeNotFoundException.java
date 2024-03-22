package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ApiTypeNotFoundException extends DataNotFoundException {

    public static final String DEFAULT_MESSAGE = "존재하지 않는 API 타입입니다.";

    public ApiTypeNotFoundException(String message) {
        super(message);
    }

    public static in.koreatech.koin.domain.version.exception.VersionTypeNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new in.koreatech.koin.domain.version.exception.VersionTypeNotFoundException(message);
    }
}
