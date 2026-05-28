package in.koreatech.koin.domain.weather.exception;

import in.koreatech.koin.global.exception.custom.ExternalServiceException;

public class WeatherOpenApiException extends ExternalServiceException {

    private static final String DEFAULT_MESSAGE = "기상청 단기예보 API 응답이 정상적이지 않습니다.";

    public WeatherOpenApiException(String message) {
        super(message);
    }

    public WeatherOpenApiException(String message, String detail) {
        super(message, detail);
    }

    public static WeatherOpenApiException withDetail(String detail) {
        return new WeatherOpenApiException(DEFAULT_MESSAGE, detail);
    }
}
