package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class AbtestVariableNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 AbtestVariable입니다.";

    public AbtestVariableNotFoundException(String message) {
        super(message);
    }

    public AbtestVariableNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestVariableNotFoundException withDetail(String detail) {
        return new AbtestVariableNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
