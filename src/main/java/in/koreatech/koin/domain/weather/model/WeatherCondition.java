package in.koreatech.koin.domain.weather.model;

import java.util.Map;

import lombok.Getter;

@Getter
public enum WeatherCondition {
    SUNNY(1, "맑음", "https://static.koreatech.in/weather/sunny.png"),
    CLOUDY(2, "구름많음", "https://static.koreatech.in/weather/cloudy.png"),
    OVERCAST(3, "흐림", "https://static.koreatech.in/weather/overcast.png"),
    RAIN(4, "비", "https://static.koreatech.in/weather/rain.png"),
    RAIN_AND_SNOW(5, "비/눈", "https://static.koreatech.in/weather/rain-and-snow.png"),
    SNOW(6, "눈", "https://static.koreatech.in/weather/snow.png"),
    SHOWER(7, "소나기", "https://static.koreatech.in/weather/shower.png");

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

    private static final Map<String, WeatherCondition> CONDITIONS_BY_VALUE = Map.of(
        SUNNY.value, SUNNY,
        CLOUDY.value, CLOUDY,
        OVERCAST.value, OVERCAST,
        RAIN.value, RAIN,
        RAIN_AND_SNOW.value, RAIN_AND_SNOW,
        SNOW.value, SNOW,
        SHOWER.value, SHOWER
    );

    private final Integer id;
    private final String value;
    private final String iconUrl;

    WeatherCondition(Integer id, String value, String iconUrl) {
        this.id = id;
        this.value = value;
        this.iconUrl = iconUrl;
    }

    public static WeatherCondition from(String sky, String precipitationType) {
        if (precipitationType != null && !precipitationType.equals("0")) {
            return PRECIPITATION_CONDITIONS.getOrDefault(precipitationType, RAIN);
        }
        return SKY_CONDITIONS.getOrDefault(sky, OVERCAST);
    }

    public static WeatherCondition fromValue(String value) {
        if (value == null) {
            return null;
        }
        return CONDITIONS_BY_VALUE.get(value);
    }
}
