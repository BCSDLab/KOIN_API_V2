package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.ExternalServiceException;

public class BusOpenApiException extends ExternalServiceException {

    private static final String DEFAULT_MESSAGE = "버스 Open API 응답이 정상적이지 않습니다.";
    private final String detail;

    public BusOpenApiException(String message) {
        super(message);
        this.detail = null;
    }

    public BusOpenApiException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static BusOpenApiException withDetail(String detail) {
        return new BusOpenApiException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
