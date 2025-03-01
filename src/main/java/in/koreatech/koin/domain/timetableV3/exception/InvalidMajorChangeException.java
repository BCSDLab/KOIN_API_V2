package in.koreatech.koin.domain.timetableV3.exception;

import in.koreatech.koin.global.exception.KoinIllegalArgumentException;

public class InvalidMajorChangeException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "해당 년도에 존재하지 않는 전공/학부입니다.";

    public InvalidMajorChangeException(String message) {
        super(message);
    }

    public InvalidMajorChangeException(String message, String detail) {
        super(message, detail);
    }

    public static InvalidMajorChangeException withDetail(String detail) {
        return new InvalidMajorChangeException(DEFAULT_MESSAGE, detail);
    }
}
