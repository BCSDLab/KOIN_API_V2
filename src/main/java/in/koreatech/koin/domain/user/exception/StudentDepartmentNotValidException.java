package in.koreatech.koin.domain.user.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class StudentDepartmentNotValidException extends DataNotFoundException {
    private static final String DEFAULT_MESSAGE = "학생의 전공 형식이 아닙니다.";

    public StudentDepartmentNotValidException(String message) {
        super(message);
    }

    public static StudentDepartmentNotValidException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new StudentDepartmentNotValidException(message);
    }
}
