package in.koreatech.koin.domain.student.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class DepartmentNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 학과입니다.";

    public DepartmentNotFoundException(String message) {
        super(message);
    }

    protected DepartmentNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static DepartmentNotFoundException withDetail(String detail) {
        return new DepartmentNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
