package in.koreatech.koin.domain.version.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class VersionTypeNotFoundException extends DataNotFoundException {

    public static final String DEFAULT_MESSAGE = "존재하지 않는 버전 타입입니다.";

    public VersionTypeNotFoundException(String message) {
        super(message);
    }

    public static VersionTypeNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new VersionTypeNotFoundException(message);
    }

    @Override
    public String getDefaultMessage() {
        return DEFAULT_MESSAGE;
    }
}
