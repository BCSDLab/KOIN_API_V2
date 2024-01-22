package in.koreatech.koin.global.common.email.exception;

public class InvalidEmailDomainException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "유효하지 않은 이메일 도메인입니다.";

    public InvalidEmailDomainException(String message) {
        super(message);
    }

    public static InvalidEmailDomainException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new InvalidEmailDomainException(message);
    }
}
