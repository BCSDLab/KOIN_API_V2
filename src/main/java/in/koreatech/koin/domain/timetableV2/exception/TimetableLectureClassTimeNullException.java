package in.koreatech.koin.domain.timetableV2.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class TimetableLectureClassTimeNullException extends KoinIllegalArgumentException {

    public static final String DEFAULT_MESSAGE = "커스텀 강의 시간에 NULL이 들어왔습니다.";

    public TimetableLectureClassTimeNullException(String message) {
        super(message);
    }

    public TimetableLectureClassTimeNullException(String message, String detail) {
        super(message, detail);
    }

    public static TimetableLectureClassTimeNullException withDetail(String detail) {
        return new TimetableLectureClassTimeNullException(DEFAULT_MESSAGE, detail);
    }
}
