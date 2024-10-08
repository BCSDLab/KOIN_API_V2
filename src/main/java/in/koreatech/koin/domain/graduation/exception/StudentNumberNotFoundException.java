package in.koreatech.koin.domain.graduation.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class StudentNumberNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 학번입니다.";

    protected StudentNumberNotFoundException(String message) {
        super(message);
    }

    protected StudentNumberNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static StudentNumberNotFoundException withDetail(String detail) {
        return new StudentNumberNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
