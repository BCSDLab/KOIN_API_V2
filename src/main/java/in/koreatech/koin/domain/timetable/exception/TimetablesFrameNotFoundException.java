package in.koreatech.koin.domain.timetable.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class TimetablesFrameNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 시간표 정보입니다.";

    public TimetablesFrameNotFoundException(String message) { super(message); }

    public TimetablesFrameNotFoundException(String message, String detail) { super(message, detail);}

    public static TimetablesFrameNotFoundException withDetail(String detail) {
        return new TimetablesFrameNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
