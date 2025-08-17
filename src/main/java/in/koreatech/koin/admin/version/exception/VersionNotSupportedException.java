package in.koreatech.koin.admin.version.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class VersionNotSupportedException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "지원하지 않는 타입입니다";

    public VersionNotSupportedException(String message) {
        super(message);
    }

    public VersionNotSupportedException(String message, String detail) {
        super(message, detail);
    }

    public static VersionNotSupportedException withDetail(String detail) {
        return new VersionNotSupportedException(DEFAULT_MESSAGE, detail);
    }
}
