package in.koreatech.koin.domain.club.exception;

import in.koreatech.koin.global.exception.custom.KoinException;

public class ClubManagerAlreadyException extends KoinException {
    private static final String DEFAULT_MESSAGE = "이미 동아리의 관리자입니다.";

    public ClubManagerAlreadyException(String message) {
        super(message);
    }

    public ClubManagerAlreadyException(String message, String detail) {
        super(message, detail);
    }

    public static ClubManagerAlreadyException withDetail(String detail) {
        return new ClubManagerAlreadyException(DEFAULT_MESSAGE, detail);
    }
}
