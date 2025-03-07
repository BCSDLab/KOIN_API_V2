package in.koreatech.koin.domain.user.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class UserGenderNotValidException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "잘못된 성별 인덱스입니다.";

    public UserGenderNotValidException(String message) {
        super(message);
    }

    public UserGenderNotValidException(String message, String detail) {
        super(message, detail);
    }

    public static UserGenderNotValidException withDetail(String detail) {
        return new UserGenderNotValidException(DEFAULT_MESSAGE, detail);
    }
}
