package in.koreatech.koin.domain.weather.model;

import java.util.Map;

import lombok.Getter;

@Getter
public enum WeatherCondition {
    SUNNY("맑음"),
    CLOUDY("구름많음"),
    OVERCAST("흐림"),
    RAIN("비"),
    RAIN_AND_SNOW("비/눈"),
    SNOW("눈"),
    SHOWER("소나기");

    private static final Map<String, WeatherCondition> SKY_CONDITIONS = Map.of(
        "1", SUNNY,
        "3", CLOUDY,
        "4", OVERCAST
    );

    private static final Map<String, WeatherCondition> PRECIPITATION_CONDITIONS = Map.of(
        "1", RAIN,
        "2", RAIN_AND_SNOW,
        "3", SNOW,
        "4", SHOWER
    );

    private final String value;

    WeatherCondition(String value) {
        this.value = value;
    }

    public static WeatherCondition from(String sky, String precipitationType) {
        if (precipitationType != null && !precipitationType.equals("0")) {
            return PRECIPITATION_CONDITIONS.getOrDefault(precipitationType, RAIN);
        }
        return SKY_CONDITIONS.getOrDefault(sky, OVERCAST);
    }
}
