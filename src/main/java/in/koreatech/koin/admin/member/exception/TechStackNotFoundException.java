package in.koreatech.koin.admin.member.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class TechStackNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 기술스택입니다.";

    protected TechStackNotFoundException(String message) {
        super(message);
    }

    protected TechStackNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static TechStackNotFoundException withDetail(String detail) {
        return new TechStackNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
