package in.koreatech.koin.domain.user.exception;

import in.koreatech.koin._common.exception.custom.DuplicationException;

public class DuplicationNicknameException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 존재하는 닉네임입니다.";

    public DuplicationNicknameException(String message) {
        super(message);
    }

    public DuplicationNicknameException(String message, String detail) {
        super(message, detail);
    }

    public static DuplicationNicknameException withDetail(String detail) {
        return new DuplicationNicknameException(DEFAULT_MESSAGE, detail);
    }
}
