package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class AbtestVariableIllegalArgumentException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "실험군이 잘못되었습니다.";

    public AbtestVariableIllegalArgumentException(String message) {
        super(message);
    }

    public AbtestVariableIllegalArgumentException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestVariableIllegalArgumentException withDetail(String detail) {
        return new AbtestVariableIllegalArgumentException(DEFAULT_MESSAGE, detail);
    }
}
