package in.koreatech.koin.domain.coop.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class StartDateAfterEndDateException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "시작일은 종료일 이전으로 설정해주세요.";

    public StartDateAfterEndDateException(String message) {
        super(message);
    }

    public StartDateAfterEndDateException(String message, String detail) {
        super(message, detail);
    }

    public static StartDateAfterEndDateException withDetail(String detail) {
        return new StartDateAfterEndDateException(DEFAULT_MESSAGE, detail);
    }
}
