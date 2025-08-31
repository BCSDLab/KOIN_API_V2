package in.koreatech.koin.domain.timetableV2.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class TimetableLectureNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 수업입니다.";

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
