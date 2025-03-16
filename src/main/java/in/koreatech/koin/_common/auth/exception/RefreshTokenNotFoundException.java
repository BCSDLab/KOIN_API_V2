package in.koreatech.koin._common.auth.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class RefreshTokenNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "리프레쉬 토큰을 찾을 수 없습니다.";

    public RefreshTokenNotFoundException(String message) {
        super(message);
    }

    public RefreshTokenNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static RefreshTokenNotFoundException withDetail(String detail) {
        return new RefreshTokenNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
