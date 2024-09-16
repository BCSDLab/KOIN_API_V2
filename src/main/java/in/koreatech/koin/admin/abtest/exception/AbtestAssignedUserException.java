package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin.global.exception.KoinIllegalArgumentException;

public class AbtestAssignedUserException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "이미 편입된 사용자입니다.";

    public AbtestAssignedUserException(String message) {
        super(message);
    }

    public AbtestAssignedUserException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestAssignedUserException withDetail(String detail) {
        return new AbtestAssignedUserException(DEFAULT_MESSAGE, detail);
    }
}
