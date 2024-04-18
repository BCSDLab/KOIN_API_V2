package in.koreatech.koin.global.domain.email.exception;

public class EmailDomainInvalidException extends IllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "유효하지 않는 이메일 도메인입니다.";

    public EmailDomainInvalidException(String message) {
        super(message);
    }

    public static EmailAddressInvalidException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new EmailAddressInvalidException(message);
    }
}