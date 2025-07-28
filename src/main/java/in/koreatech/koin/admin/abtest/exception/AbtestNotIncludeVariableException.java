package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class AbtestNotIncludeVariableException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "요청된 AB테스트 내에 실험군이 존재하지 않습니다.";

    public AbtestNotIncludeVariableException(String message) {
        super(message);
    }

    public AbtestNotIncludeVariableException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestNotIncludeVariableException withDetail(String detail) {
        return new AbtestNotIncludeVariableException(DEFAULT_MESSAGE, detail);
    }
}
