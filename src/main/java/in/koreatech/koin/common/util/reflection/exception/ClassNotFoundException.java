package in.koreatech.koin.common.util.reflection.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class ClassNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 클래스 타입 입니다.";

    public ClassNotFoundException(String message) {
        super(message);
    }

    public ClassNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ClassNotFoundException withDetail(String detail) {
        return new ClassNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
