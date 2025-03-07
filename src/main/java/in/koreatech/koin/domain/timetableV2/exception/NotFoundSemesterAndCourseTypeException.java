package in.koreatech.koin.domain.timetableV2.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class NotFoundSemesterAndCourseTypeException extends DataNotFoundException {
    private static final String DEFAULT_MESSAGE = "학기나 이수구분을 찾을 수 없습니다.";

    public NotFoundSemesterAndCourseTypeException(String message) {
        super(message);
    }

    public NotFoundSemesterAndCourseTypeException(String message, String detail) {
        super(message, detail);
    }

    public static NotFoundSemesterAndCourseTypeException withDetail(String detail) {
        return new NotFoundSemesterAndCourseTypeException(DEFAULT_MESSAGE, detail);
    }
}
