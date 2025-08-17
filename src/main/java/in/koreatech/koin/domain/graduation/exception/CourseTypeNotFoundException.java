package in.koreatech.koin.domain.graduation.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class CourseTypeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 이수 구분입니다.";

    protected CourseTypeNotFoundException(String message) {
        super(message);
    }

    protected CourseTypeNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static CourseTypeNotFoundException withDetail(String detail) {
        return new CourseTypeNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
