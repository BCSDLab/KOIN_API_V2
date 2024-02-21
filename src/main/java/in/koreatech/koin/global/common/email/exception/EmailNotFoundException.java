package in.koreatech.koin.global.common.email.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class EmailNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 이메일입니다.";

    public EmailNotFoundException(String message) {
        super(message);
    }

    public static EmailNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new EmailNotFoundException(message);
    }
}
