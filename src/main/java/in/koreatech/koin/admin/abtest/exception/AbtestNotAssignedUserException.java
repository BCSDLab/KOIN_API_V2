package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class AbtestNotAssignedUserException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "편입되지 않은 사용자입니다.";

    public AbtestNotAssignedUserException(String message) {
        super(message);
    }

    public AbtestNotAssignedUserException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestNotAssignedUserException withDetail(String detail) {
        return new AbtestNotAssignedUserException(DEFAULT_MESSAGE, detail);
    }
}
