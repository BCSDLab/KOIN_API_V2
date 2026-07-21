package in.koreatech.koin.domain.weather.exception;

public class WeatherOpenApiRateLimitException extends WeatherOpenApiException {

    private WeatherOpenApiRateLimitException(String detail) {
        super(DEFAULT_MESSAGE, detail);
    }

    public static WeatherOpenApiRateLimitException withDetail(String detail) {
        return new WeatherOpenApiRateLimitException(detail);
    }
}
