package in.koreatech.koin.domain.timetable.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class SemesterNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 학기입니다.";

    public SemesterNotFoundException(String message) {
        super(message);
    }

    public SemesterNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static SemesterNotFoundException withDetail(String detail) {
        return new SemesterNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
