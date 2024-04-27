package in.koreatech.koin.domain.member.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class MemberNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 BCSD 회원입니다.";
    private final String detail;

    public MemberNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public MemberNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static MemberNotFoundException withDetail(String detail) {
        return new MemberNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
