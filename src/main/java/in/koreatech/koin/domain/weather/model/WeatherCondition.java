package in.koreatech.koin.domain.weather.model;

import java.util.Map;

import lombok.Getter;

@Getter
public enum WeatherCondition {
    SUNNY(1, "맑음", "https://static.koreatech.in/upload/WEATHER/sun-03.webp"),
    CLOUDY(2, "구름많음", "https://static.koreatech.in/upload/WEATHER/sun-cloud-02.webp"),
    OVERCAST(3, "흐림", "https://static.koreatech.in/upload/WEATHER/cloud.webp"),
    RAIN(4, "비", "https://static.koreatech.in/upload/WEATHER/cloud-angled-rain.webp"),
    RAIN_AND_SNOW(5, "비/눈", "https://static.koreatech.in/upload/WEATHER/cloud-hail.webp"),
    SNOW(6, "눈", "https://static.koreatech.in/upload/WEATHER/cloud-mid-snow.webp"),
    SHOWER(7, "소나기", "https://static.koreatech.in/upload/WEATHER/cloud-rain-wind.webp");

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
