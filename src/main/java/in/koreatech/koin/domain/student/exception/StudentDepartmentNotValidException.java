package in.koreatech.koin.domain.student.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class StudentDepartmentNotValidException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "학생의 전공 형식이 아닙니다.";

    public StudentDepartmentNotValidException(String message) {
        super(message);
    }

    public StudentDepartmentNotValidException(String message, String detail) {
        super(message, detail);
    }

    public static StudentDepartmentNotValidException withDetail(String detail) {
        return new StudentDepartmentNotValidException(DEFAULT_MESSAGE, detail);
    }
}
