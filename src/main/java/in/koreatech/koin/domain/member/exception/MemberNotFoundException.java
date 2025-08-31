package in.koreatech.koin.domain.member.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class MemberNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 BCSD 회원입니다.";

    public MemberNotFoundException(String message) {
        super(message);
    }

    public MemberNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static MemberNotFoundException withDetail(String detail) {
        return new MemberNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
