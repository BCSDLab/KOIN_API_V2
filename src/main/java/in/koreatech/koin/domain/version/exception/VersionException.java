package in.koreatech.koin.domain.version.exception;

public class VersionException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "올바르지 않은 버전입니다.";

    public VersionException() {
    }

    public VersionException(String message) {
        super(message);
    }

    public static VersionException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new VersionException(message);
    }
}
