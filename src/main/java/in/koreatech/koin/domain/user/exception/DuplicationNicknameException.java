package in.koreatech.koin.domain.user.exception;

import in.koreatech.koin.global.exception.DuplicationException;

public class DuplicationNicknameException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 존재하는 닉네임입니다.";

    public DuplicationNicknameException(String message) {
        super(message);
    }

    public static DuplicationNicknameException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new DuplicationNicknameException(message);
    }
}
