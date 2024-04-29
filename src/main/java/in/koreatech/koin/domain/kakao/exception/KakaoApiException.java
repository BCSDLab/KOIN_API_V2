package in.koreatech.koin.domain.kakao.exception;

import in.koreatech.koin.global.exception.ExternalServiceException;

public class KakaoApiException extends ExternalServiceException {

    private static final String DEFAULT_MESSAGE = "카카오 API 호출 과정에서 문제가 발생했습니다.";
    private final String detail;

    public KakaoApiException(String message) {
        super(message);
        this.detail = null;
    }

    public KakaoApiException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static KakaoApiException withDetail(String detail) {
        return new KakaoApiException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
