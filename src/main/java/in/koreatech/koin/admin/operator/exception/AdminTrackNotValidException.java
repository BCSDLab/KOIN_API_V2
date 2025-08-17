package in.koreatech.koin.admin.operator.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class AdminTrackNotValidException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "트랙 형식이 아닙니다.";

    public AdminTrackNotValidException(String message) {
        super(message);
    }

    public AdminTrackNotValidException(String message, String detail) {
        super(message, detail);
    }

    public static AdminTrackNotValidException withDetail(String detail) {
        return new AdminTrackNotValidException(DEFAULT_MESSAGE, detail);
    }
}
