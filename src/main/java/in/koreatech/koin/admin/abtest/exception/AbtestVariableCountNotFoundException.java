package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class AbtestVariableCountNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 AbtestVariableCount입니다.";

    public AbtestVariableCountNotFoundException(String message) {
        super(message);
    }

    public AbtestVariableCountNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestVariableCountNotFoundException withDetail(String detail) {
        return new AbtestVariableCountNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
