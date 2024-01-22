package in.koreatech.koin.global.common.email.exception;

public class DuplicationEmailException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "존재하는 이메일입니다.";

    public DuplicationEmailException(String message) {
        super(message);
    }

    public static DuplicationEmailException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new DuplicationEmailException(message);
    }
}
