package in.koreatech.koin.domain.timetable.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class TimetableFrameNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 시간표 프레임입니다.";

    public TimetableFrameNotFoundException(String message) {
        super(message);
    }

    public TimetableFrameNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static TimetableFrameNotFoundException withDetail(String detail) {
        return new TimetableFrameNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
