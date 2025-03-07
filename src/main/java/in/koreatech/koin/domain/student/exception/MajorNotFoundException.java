package in.koreatech.koin.domain.student.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class MajorNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 전공입니다.";

    public MajorNotFoundException(String message) {
        super(message);
    }

    protected MajorNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static MajorNotFoundException withDetail(String detail) {
        return new MajorNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
