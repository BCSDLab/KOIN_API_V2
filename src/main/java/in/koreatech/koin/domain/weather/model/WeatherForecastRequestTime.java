package in.koreatech.koin.domain.weather.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record WeatherForecastRequestTime(
    String baseDate,
    String baseTime,
    String forecastDate,
    String forecastTime
) {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");
    private static final List<LocalTime> BASE_TIMES = List.of(
        LocalTime.of(2, 0),
        LocalTime.of(5, 0),
        LocalTime.of(8, 0),
        LocalTime.of(11, 0),
        LocalTime.of(14, 0),
        LocalTime.of(17, 0),
        LocalTime.of(20, 0),
        LocalTime.of(23, 0)
    );

    public static WeatherForecastRequestTime from(LocalDateTime now) {
        LocalDate baseDate = now.toLocalDate();
        LocalTime availableTime = now.toLocalTime().minusMinutes(10);
        LocalTime baseTime = null;
        for (LocalTime time : BASE_TIMES) {
            if (!time.isAfter(availableTime)) {
                baseTime = time;
            }
        }
        if (baseTime == null) {
            baseDate = baseDate.minusDays(1);
            baseTime = BASE_TIMES.get(BASE_TIMES.size() - 1);
        }

        LocalDateTime forecastDateTime = now.withMinute(0).withSecond(0).withNano(0);

        return new WeatherForecastRequestTime(
            baseDate.format(DATE_FORMATTER),
            baseTime.format(TIME_FORMATTER),
            forecastDateTime.toLocalDate().format(DATE_FORMATTER),
            forecastDateTime.toLocalTime().format(TIME_FORMATTER)
        );
    }

    public WeatherForecastRequestTime previousBaseTime() {
        LocalDate baseLocalDate = LocalDate.parse(baseDate, DATE_FORMATTER);
        LocalTime baseLocalTime = LocalTime.parse(baseTime, TIME_FORMATTER);
        int baseTimeIndex = BASE_TIMES.indexOf(baseLocalTime);
        if (baseTimeIndex <= 0) {
            baseLocalDate = baseLocalDate.minusDays(1);
            baseLocalTime = BASE_TIMES.get(BASE_TIMES.size() - 1);
        } else {
            baseLocalTime = BASE_TIMES.get(baseTimeIndex - 1);
        }

        return new WeatherForecastRequestTime(
            baseLocalDate.format(DATE_FORMATTER),
            baseLocalTime.format(TIME_FORMATTER),
            forecastDate,
            forecastTime
        );
    }
}
