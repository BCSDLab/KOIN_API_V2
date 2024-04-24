package in.koreatech.koin.domain.member.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class MemberNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 BCSD 회원입니다.";

    public MemberNotFoundException(String message) {
        super(message);
    }

    public static MemberNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new MemberNotFoundException(message);
    }

    @Override
    public String getDefaultMessage() {
        return DEFAULT_MESSAGE;
    }
}
