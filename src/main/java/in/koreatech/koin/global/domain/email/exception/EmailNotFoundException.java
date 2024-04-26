package in.koreatech.koin.global.domain.email.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class EmailNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 이메일입니다.";
    private final String detail;

    public EmailNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public EmailNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static EmailNotFoundException withDetail(String detail) {
        return new EmailNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
