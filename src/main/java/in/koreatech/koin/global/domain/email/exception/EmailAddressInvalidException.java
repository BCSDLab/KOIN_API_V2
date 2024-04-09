package in.koreatech.koin.global.domain.email.exception;

public class EmailAddressInvalidException extends IllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "유효하지 않는 이메일 주소입니다.";

    public EmailAddressInvalidException(String message) {
        super(message);
    }

    public static EmailAddressInvalidException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new EmailAddressInvalidException(message);
    }
}
