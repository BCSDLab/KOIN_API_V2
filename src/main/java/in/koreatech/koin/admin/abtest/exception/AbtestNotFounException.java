package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class AbtestNotFounException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 Abtest입니다.";

    public AbtestNotFounException(String message) {
        super((message));
    }

    public AbtestNotFounException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestNotFounException withDetail(String detail) {
        return new AbtestNotFounException(DEFAULT_MESSAGE, detail);
    }
}
