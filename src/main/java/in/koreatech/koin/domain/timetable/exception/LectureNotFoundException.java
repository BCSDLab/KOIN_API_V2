package in.koreatech.koin.domain.timetable.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class LectureNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 강의입니다.";

    public LectureNotFoundException(String message) {
        super(message);
    }

    public LectureNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static LectureNotFoundException withDetail(String detail) {
        return new LectureNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
