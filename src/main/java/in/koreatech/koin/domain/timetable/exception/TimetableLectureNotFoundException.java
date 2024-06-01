package in.koreatech.koin.domain.timetable.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class TimetableLectureNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 시간표입니다.";

    public TimetableLectureNotFoundException(String message) {
        super(message);
    }

    public TimetableLectureNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static TimetableLectureNotFoundException withDetail(String detail) {
        return new TimetableLectureNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
