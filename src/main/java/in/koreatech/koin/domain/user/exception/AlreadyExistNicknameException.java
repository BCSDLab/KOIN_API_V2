package in.koreatech.koin.domain.user.exception;

import in.koreatech.koin.global.exception.AlreadyExistDataException;

public class AlreadyExistNicknameException extends AlreadyExistDataException {

    private static final String DEFAULT_MESSAGE = "이미 존재하는 닉네임입니다.";

    public AlreadyExistNicknameException(String message) {
        super(message);
    }

    public static AlreadyExistNicknameException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new AlreadyExistNicknameException(message);
    }
}
