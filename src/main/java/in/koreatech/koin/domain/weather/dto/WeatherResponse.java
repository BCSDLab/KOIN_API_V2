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

    @Schema(
        description = "날씨 상태. 가능한 값: 맑음, 구름많음, 흐림, 비, 비/눈, 눈, 소나기",
        example = "맑음",
        allowableValues = {"맑음", "구름많음", "흐림", "비", "비/눈", "눈", "소나기"},
        requiredMode = REQUIRED
    )
    String weather,

    @Schema(
        description = "날씨 상태 ID. 1: 맑음, 2: 구름많음, 3: 흐림, 4: 비, 5: 비/눈, 6: 눈, 7: 소나기",
        example = "1",
        allowableValues = {"1", "2", "3", "4", "5", "6", "7"},
        requiredMode = REQUIRED
    )
    Integer weatherId,

    @Schema(
        description = "날씨 아이콘 URL",
        example = "https://static.koreatech.in/weather/sunny.png",
        requiredMode = REQUIRED
    )
    String weatherIconUrl
) {

    public WeatherResponse(Integer temperature, String weather) {
        this(temperature, weather, null, null);
    }

    public WeatherResponse {
        WeatherCondition weatherCondition = WeatherCondition.fromValue(weather);
        if (weatherCondition != null && (weatherId == null || weatherIconUrl == null)) {
            weatherId = weatherCondition.getId();
            weatherIconUrl = weatherCondition.getIconUrl();
        }
    }

    public static WeatherResponse of(Integer temperature, WeatherCondition weatherCondition) {
        return new WeatherResponse(
            temperature,
            weatherCondition.getValue(),
            weatherCondition.getId(),
            weatherCondition.getIconUrl()
        );
    }
}
