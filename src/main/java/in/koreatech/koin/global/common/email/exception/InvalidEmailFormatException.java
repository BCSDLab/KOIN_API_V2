package in.koreatech.koin.global.common.email.exception;

public class InvalidEmailFormatException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "유효하지 않은 이메일 형식입니다.";

    public InvalidEmailFormatException(String message) {
        super(message);
    }

    public static InvalidEmailFormatException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new InvalidEmailFormatException(message);
    }
}
