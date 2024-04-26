package in.koreatech.koin.domain.timetable.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class SemesterNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 학기입니다.";
    private final String detail;

    public SemesterNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public SemesterNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static SemesterNotFoundException withDetail(String detail) {
        return new SemesterNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
