package in.koreatech.koin.domain.version.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class VersionTypeNotFoundException extends DataNotFoundException {

    public static final String DEFAULT_MESSAGE = "존재하지 않는 버전 타입입니다.";

    public VersionTypeNotFoundException(String message) {
        super(message);
    }

    public VersionTypeNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static VersionTypeNotFoundException withDetail(String detail) {
        return new VersionTypeNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
