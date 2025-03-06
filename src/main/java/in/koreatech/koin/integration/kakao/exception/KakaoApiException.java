package in.koreatech.koin.integration.kakao.exception;

import in.koreatech.koin._common.exception.custom.ExternalServiceException;

public class KakaoApiException extends ExternalServiceException {

    private static final String DEFAULT_MESSAGE = "카카오 API 호출 과정에서 문제가 발생했습니다.";

    public KakaoApiException(String message) {
        super(message);
    }

    public KakaoApiException(String message, String detail) {
        super(message, detail);
    }

    public static KakaoApiException withDetail(String detail) {
        return new KakaoApiException(DEFAULT_MESSAGE, detail);
    }
}
