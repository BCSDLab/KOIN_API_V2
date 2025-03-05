package in.koreatech.koin.domain.timetableV3.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ChangeMajorNotExistException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 년도에 존재하지 않는 전공/학부입니다.";

    public ChangeMajorNotExistException(String message) {
        super(message);
    }

    public ChangeMajorNotExistException(String message, String detail) {
        super(message, detail);
    }

    public static ChangeMajorNotExistException withDetail(String detail) {
        return new ChangeMajorNotExistException(DEFAULT_MESSAGE, detail);
    }
}
