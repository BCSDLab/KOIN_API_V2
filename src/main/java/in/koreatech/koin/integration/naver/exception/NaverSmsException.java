package in.koreatech.koin.integration.naver.exception;

import in.koreatech.koin._common.exception.custom.ExternalServiceException;

public class NaverSmsException extends ExternalServiceException {

    private static final String DEFAULT_MESSAGE = "Naver SENS API 호출 과정에서 문제가 발생했습니다.";

    public NaverSmsException(String message) {
        super(message);
    }

    public NaverSmsException(String message, String detail) {
        super(message, detail);
    }

    public static NaverSmsException withDetail(String detail) {
        return new NaverSmsException(DEFAULT_MESSAGE, detail);
    }
}
