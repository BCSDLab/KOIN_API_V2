package in.koreatech.koin.common.util.reflection.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class MethodNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 메서드 입니다.";

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
