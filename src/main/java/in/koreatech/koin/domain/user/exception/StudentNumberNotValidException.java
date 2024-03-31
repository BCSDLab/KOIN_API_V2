package in.koreatech.koin.domain.user.exception;

import in.koreatech.koin.global.exception.DataInvalidException;

public class StudentNumberNotValidException extends DataInvalidException {

    private static final String DEFAULT_MESSAGE = "학생의 학번 형식이 아닙니다.";

    public StudentNumberNotValidException(String message) {
        super(message);
    }

    public static StudentNumberNotValidException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new StudentNumberNotValidException(message);
    }
}
