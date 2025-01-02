package in.koreatech.koin.domain.timetableV3.exception;

import in.koreatech.koin.global.exception.KoinIllegalArgumentException;

public class TimetableLectureTimeDuplicateException extends KoinIllegalArgumentException {
    public static final String DEFAULT_MESSAGE = "강의 시간이 겹칩니다.";

    public TimetableLectureTimeDuplicateException(String message) {
        super(message);
    }

    public TimetableLectureTimeDuplicateException(String message, String detail) {
        super(message, detail);
    }

    public static TimetableLectureTimeDuplicateException withDetail(String detail) {
        return new TimetableLectureTimeDuplicateException(DEFAULT_MESSAGE, detail);
    }
}
