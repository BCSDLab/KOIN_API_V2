package in.koreatech.koin.domain.timetable.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class TimeTableNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 시간표입니다.";

    public TimeTableNotFoundException(String message) {
        super(message);
    }

    public TimeTableNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static TimeTableNotFoundException withDetail(String detail) {
        return new TimeTableNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
