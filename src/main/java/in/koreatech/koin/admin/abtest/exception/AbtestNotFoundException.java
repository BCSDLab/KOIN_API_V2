package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class AbtestNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 Abtest입니다.";

    public AbtestNotFoundException(String message) {
        super(message);
    }

    public AbtestNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestNotFoundException withDetail(String detail) {
        return new AbtestNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
