package in.koreatech.koin.unit.domain.weather;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.weather.model.WeatherForecastRequestTime;

class WeatherForecastRequestTimeTest {

    @Test
    void 기상청_단기예보_제공시각이_지난_가장_최근_발표시각을_사용한다() {
        WeatherForecastRequestTime requestTime = WeatherForecastRequestTime.from(
            LocalDateTime.of(2024, 1, 15, 12, 35)
        );

        assertThat(requestTime.baseDate()).isEqualTo("20240115");
        assertThat(requestTime.baseTime()).isEqualTo("1100");
        assertThat(requestTime.forecastDate()).isEqualTo("20240115");
        assertThat(requestTime.forecastTime()).isEqualTo("1200");
    }

    @Test
    void 첫_발표시각_전에는_전날_마지막_발표시각을_사용한다() {
        WeatherForecastRequestTime requestTime = WeatherForecastRequestTime.from(
            LocalDateTime.of(2024, 1, 15, 2, 5)
        );

        assertThat(requestTime.baseDate()).isEqualTo("20240114");
        assertThat(requestTime.baseTime()).isEqualTo("2300");
        assertThat(requestTime.forecastDate()).isEqualTo("20240115");
        assertThat(requestTime.forecastTime()).isEqualTo("0200");
    }

    @Test
    void 자정_직후에는_전날_마지막_발표시각을_사용한다() {
        WeatherForecastRequestTime requestTime = WeatherForecastRequestTime.from(
            LocalDateTime.of(2024, 1, 15, 0, 5)
        );

        assertThat(requestTime.baseDate()).isEqualTo("20240114");
        assertThat(requestTime.baseTime()).isEqualTo("2300");
        assertThat(requestTime.forecastDate()).isEqualTo("20240115");
        assertThat(requestTime.forecastTime()).isEqualTo("0000");
    }

    @Test
    void 직전_발표시각을_계산한다() {
        WeatherForecastRequestTime requestTime = new WeatherForecastRequestTime(
            "20240115",
            "1100",
            "20240115",
            "1200"
        );

        WeatherForecastRequestTime previousBaseTime = requestTime.previousBaseTime();

        assertThat(previousBaseTime.baseDate()).isEqualTo("20240115");
        assertThat(previousBaseTime.baseTime()).isEqualTo("0800");
        assertThat(previousBaseTime.forecastDate()).isEqualTo("20240115");
        assertThat(previousBaseTime.forecastTime()).isEqualTo("1200");
    }

    @Test
    void 첫_발표시각의_직전_발표시각은_전날_마지막_발표시각이다() {
        WeatherForecastRequestTime requestTime = new WeatherForecastRequestTime(
            "20240115",
            "0200",
            "20240115",
            "0300"
        );

        WeatherForecastRequestTime previousBaseTime = requestTime.previousBaseTime();

        assertThat(previousBaseTime.baseDate()).isEqualTo("20240114");
        assertThat(previousBaseTime.baseTime()).isEqualTo("2300");
        assertThat(previousBaseTime.forecastDate()).isEqualTo("20240115");
        assertThat(previousBaseTime.forecastTime()).isEqualTo("0300");
    }
}
