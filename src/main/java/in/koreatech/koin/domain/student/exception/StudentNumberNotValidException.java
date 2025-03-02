package in.koreatech.koin.domain.student.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class StudentNumberNotValidException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "학생의 학번 형식이 아닙니다.";

    public StudentNumberNotValidException(String message) {
        super(message);
    }

    public StudentNumberNotValidException(String message, String detail) {
        super(message, detail);
    }

    public static StudentNumberNotValidException withDetail(String detail) {
        return new StudentNumberNotValidException(DEFAULT_MESSAGE, detail);
    }
}
