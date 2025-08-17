package in.koreatech.koin.admin.user.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class AdminTeamNotValidException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "팀 형식이 아닙니다.";

    public AdminTeamNotValidException(String message) {
        super(message);
    }

    public AdminTeamNotValidException(String message, String detail) {
        super(message, detail);
    }

    public static AdminTeamNotValidException withDetail(String detail) {
        return new AdminTeamNotValidException(DEFAULT_MESSAGE, detail);
    }
}
