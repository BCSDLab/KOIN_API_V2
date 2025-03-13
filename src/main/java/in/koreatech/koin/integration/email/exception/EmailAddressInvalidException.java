package in.koreatech.koin.integration.email.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class EmailAddressInvalidException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "유효하지 않는 이메일 주소입니다.";

    public EmailAddressInvalidException(String message) {
        super(message);
    }

    public EmailAddressInvalidException(String message, String detail) {
        super(message, detail);
    }

    public static EmailAddressInvalidException withDetail(String detail) {
        return new EmailAddressInvalidException(DEFAULT_MESSAGE, detail);
    }
}
