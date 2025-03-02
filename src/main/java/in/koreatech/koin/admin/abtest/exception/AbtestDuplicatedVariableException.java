package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin._common.exception.custom.DuplicationException;

public class AbtestDuplicatedVariableException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "실험군이 중복됩니다.";

    public AbtestDuplicatedVariableException(String message) {
        super(message);
    }

    public AbtestDuplicatedVariableException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestDuplicatedVariableException withDetail(String detail) {
        return new AbtestDuplicatedVariableException(DEFAULT_MESSAGE, detail);
    }
}
