package in.koreatech.koin.domain.weather.model;

import in.koreatech.koin.domain.weather.dto.WeatherResponse;

public record WeatherForecast(
    Integer temperature,
    String sky,
    String precipitationType
) {

    public WeatherResponse toResponse() {
        return WeatherResponse.of(
            temperature,
            WeatherCondition.from(sky, precipitationType)
        );
    }
}
