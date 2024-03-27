package in.koreatech.koin.domain.user.exception;

public class StudentDepartmentNotValidException extends IllegalArgumentException {
    private static final String DEFAULT_MESSAGE = "학생의 전공 형식이 아닙니다.";

    public StudentDepartmentNotValidException(String message) {
        super(message);
    }

    public static StudentDepartmentNotValidException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new StudentDepartmentNotValidException(message);
    }
}
