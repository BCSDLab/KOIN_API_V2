package in.koreatech.koin.domain.version.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class VersionTypeNotFoundException extends DataNotFoundException {

    public static final String DEFAULT_MESSAGE = "존재하지 않는 버전 타입입니다.";
    private final String detail;

    public VersionTypeNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public VersionTypeNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static VersionTypeNotFoundException withDetail(String detail) {
        return new VersionTypeNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
