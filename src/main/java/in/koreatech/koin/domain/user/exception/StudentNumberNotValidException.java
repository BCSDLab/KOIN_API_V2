package in.koreatech.koin.domain.user.exception;

public class StudentNumberNotValidException extends IllegalArgumentException {
    private static final String DEFAULT_MESSAGE = "학생의 학번 형식이 아닙니다.";

    public StudentNumberNotValidException(String message) {
        super(message);
    }

    public static StudentNumberNotValidException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new StudentNumberNotValidException(message);
    }
}
