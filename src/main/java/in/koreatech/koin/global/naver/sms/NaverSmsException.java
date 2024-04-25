package in.koreatech.koin.global.naver.sms;

import in.koreatech.koin.global.exception.ExternalServiceException;

public class NaverSmsException extends ExternalServiceException {

    private static final String DEFAULT_MESSAGE = "Naver SENS API 호출 과정에서 문제가 발생했습니다.";

    public NaverSmsException(String message) {
        super(message);
    }

    public static NaverSmsException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new NaverSmsException(message);
    }
}
