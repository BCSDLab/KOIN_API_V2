package in.koreatech.koin.domain.timetableV3.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class InvalidTermFormatException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "term 양식이 잘못됐습니다.";

    public InvalidTermFormatException(String message) {
        super(message);
    }

    public InvalidTermFormatException(String message, String detail) {
        super(message, detail);
    }

    public static InvalidTermFormatException withDetail(String detail) {
        return new InvalidTermFormatException(DEFAULT_MESSAGE, detail);
    }
}
