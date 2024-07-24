package in.koreatech.koin.global.domain.callcontol.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class MethodNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "잘못된 이름의 메서드 입니다.";

    public MethodNotFoundException(String message) {
        super(message);
    }

    public MethodNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static MethodNotFoundException withDetail(String detail) {
        return new MethodNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
