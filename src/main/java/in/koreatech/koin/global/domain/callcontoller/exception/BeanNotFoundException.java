package in.koreatech.koin.global.domain.callcontoller.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class BeanNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "등록되지 않은 Bean 입니다.";

    public BeanNotFoundException(String message) {
        super(message);
    }

    public BeanNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static BeanNotFoundException withDetail(String detail) {
        return new BeanNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
