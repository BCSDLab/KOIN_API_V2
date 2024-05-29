package in.koreatech.koin.admin.member.exception;

import in.koreatech.koin.global.exception.KoinIllegalStateException;

public class MemberAlreadyDeletedException extends KoinIllegalStateException {

    private static final String DEFAULT_MESSAGE = "학생의 전공 형식이 아닙니다.";

    public MemberAlreadyDeletedException(String message) {
        super(message);
    }

    public MemberAlreadyDeletedException(String message, String detail) {
        super(message, detail);
    }

    public static MemberAlreadyDeletedException withDetail(String detail) {
        return new MemberAlreadyDeletedException(DEFAULT_MESSAGE, detail);
    }
}
