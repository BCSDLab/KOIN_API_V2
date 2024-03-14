package in.koreatech.koin.domain.timetable.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class SemesterNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 학기입니다.";

    public SemesterNotFoundException(String message) {
        super(message);
    }

    public static SemesterNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new SemesterNotFoundException(message);
    }
}
