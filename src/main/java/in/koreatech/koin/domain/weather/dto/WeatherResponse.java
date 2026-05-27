package in.koreatech.koin.domain.weather.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.weather.model.WeatherCondition;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record WeatherResponse(
    @Schema(description = "기온(섭씨)", example = "21", requiredMode = REQUIRED)
    Integer temperature,

    @Schema(description = "날씨 상태", example = "맑음", requiredMode = REQUIRED)
    String weather
) {

    public static WeatherResponse of(Integer temperature, WeatherCondition weatherCondition) {
        return new WeatherResponse(temperature, weatherCondition.getValue());
    }
}
