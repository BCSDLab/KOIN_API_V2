package in.koreatech.koin.domain.timetable.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class TimetableNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 시간표입니다.";

    public TimetableNotFoundException(String message) {
        super(message);
    }

    public TimetableNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static TimetableNotFoundException withDetail(String detail) {
        return new TimetableNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
