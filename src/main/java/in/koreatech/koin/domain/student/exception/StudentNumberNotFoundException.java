package in.koreatech.koin.domain.student.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class StudentNumberNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "학번을 추가하세요.";

    public StudentNumberNotFoundException
        (String message) {
        super(message);
    }

    protected StudentNumberNotFoundException
        (String message, String detail) {
        super(message, detail);
    }

    public static StudentNumberNotFoundException
    withDetail(String detail) {
        return new StudentNumberNotFoundException
            (DEFAULT_MESSAGE, detail);
    }
}
