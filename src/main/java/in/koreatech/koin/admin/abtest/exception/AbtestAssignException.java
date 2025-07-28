package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;

public class AbtestAssignException extends KoinIllegalStateException {

    private static final String DEFAULT_MESSAGE = "AB테스트 편입 중 오류가 발생했습니다.";

    public AbtestAssignException(String message) {
        super(message);
    }

    public AbtestAssignException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestAssignException withDetail(String detail) {
        return new AbtestAssignException(DEFAULT_MESSAGE, detail);
    }
}
