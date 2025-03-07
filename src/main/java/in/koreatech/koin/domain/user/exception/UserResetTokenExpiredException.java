package in.koreatech.koin.domain.user.exception;

import in.koreatech.koin._common.auth.exception.AuthenticationException;

public class UserResetTokenExpiredException extends AuthenticationException {

    private static final String DEFAULT_MESSAGE = "비밀번호 재설정 토큰이 만료되었습니다.";

    public UserResetTokenExpiredException(String message) {
        super(message);
    }

    public UserResetTokenExpiredException(String message, String detail) {
        super(message, detail);
    }

    public static UserResetTokenExpiredException withDetail(String detail) {
        return new UserResetTokenExpiredException(DEFAULT_MESSAGE, detail);
    }
}
