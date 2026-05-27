package in.koreatech.koin.unit.domain.weather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.weather.client.WeatherClient;
import in.koreatech.koin.domain.weather.dto.WeatherResponse;
import in.koreatech.koin.domain.weather.model.WeatherForecast;
import in.koreatech.koin.domain.weather.model.WeatherForecastRequestTime;
import in.koreatech.koin.domain.weather.service.WeatherService;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeatherClient weatherClient;

    @Test
    void 캐시가_유효하면_기상청_API를_다시_호출하지_않는다() {
        Clock clock = Clock.fixed(Instant.parse("2024-01-15T03:35:00Z"), ZoneId.of("Asia/Seoul"));
        WeatherService weatherService = new WeatherService(clock, weatherClient);
        WeatherForecastRequestTime requestTime = new WeatherForecastRequestTime(
            "20240115",
            "1100",
            "20240115",
            "1200"
        );
        when(weatherClient.getWeatherForecast(requestTime))
            .thenReturn(new WeatherForecast(21, "1", "0"));

        WeatherResponse firstResponse = weatherService.getWeather();
        WeatherResponse secondResponse = weatherService.getWeather();

        assertThat(firstResponse).isSameAs(secondResponse);
        verify(weatherClient, times(1)).getWeatherForecast(requestTime);
    }
}
