package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.custom.ExternalServiceException;

public class BusOpenApiException extends ExternalServiceException {

    private static final String DEFAULT_MESSAGE = "버스 Open API 응답이 정상적이지 않습니다.";

    public BusOpenApiException(String message) {
        super(message);
    }

    public BusOpenApiException(String message, String detail) {
        super(message, detail);
    }

    public static BusOpenApiException withDetail(String detail) {
        return new BusOpenApiException(DEFAULT_MESSAGE, detail);
    }
}
