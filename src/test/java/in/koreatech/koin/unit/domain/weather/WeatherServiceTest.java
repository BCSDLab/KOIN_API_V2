package in.koreatech.koin.unit.domain.weather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.weather.client.WeatherClient;
import in.koreatech.koin.domain.weather.dto.WeatherResponse;
import in.koreatech.koin.domain.weather.exception.WeatherOpenApiException;
import in.koreatech.koin.domain.weather.model.WeatherCache;
import in.koreatech.koin.domain.weather.model.WeatherForecast;
import in.koreatech.koin.domain.weather.model.WeatherForecastRequestTime;
import in.koreatech.koin.domain.weather.repository.WeatherCacheRepository;
import in.koreatech.koin.domain.weather.service.WeatherService;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeatherClient weatherClient;

    @Mock
    private WeatherCacheRepository weatherCacheRepository;

    @Test
    void 레디스_캐시가_있으면_기상청_API를_호출하지_않고_캐시를_반환한다() {
        Clock clock = Clock.fixed(Instant.parse("2024-01-15T03:35:00Z"), ZoneId.of("Asia/Seoul"));
        WeatherService weatherService = new WeatherService(clock, weatherClient, weatherCacheRepository);
        WeatherForecastRequestTime requestTime = new WeatherForecastRequestTime(
            "20240115",
            "1100",
            "20240115",
            "1200"
        );
        WeatherResponse cachedWeather = new WeatherResponse(21, "맑음");
        when(weatherCacheRepository.findById(WeatherCache.BYEONGCHEON_ID))
            .thenReturn(Optional.of(WeatherCache.of(cachedWeather)));

        WeatherResponse response = weatherService.getWeather();

        assertThat(response).isEqualTo(cachedWeather);
        verify(weatherClient, never()).getWeatherForecast(requestTime);
    }

    @Test
    void 레디스_캐시가_없어도_조회_API에서는_기상청_API를_호출하지_않는다() {
        Clock clock = Clock.fixed(Instant.parse("2024-01-15T03:35:00Z"), ZoneId.of("Asia/Seoul"));
        WeatherService weatherService = new WeatherService(clock, weatherClient, weatherCacheRepository);
        when(weatherCacheRepository.findById(WeatherCache.BYEONGCHEON_ID))
            .thenReturn(Optional.empty());

        assertThrows(WeatherOpenApiException.class, weatherService::getWeather);

        verify(weatherClient, never()).getWeatherForecast(any());
    }

    @Test
    void 스케줄러_갱신에서는_기상청_API를_호출하고_캐시를_저장한다() {
        Clock clock = Clock.fixed(Instant.parse("2024-01-15T03:35:00Z"), ZoneId.of("Asia/Seoul"));
        WeatherService weatherService = new WeatherService(clock, weatherClient, weatherCacheRepository);
        WeatherForecastRequestTime requestTime = new WeatherForecastRequestTime(
            "20240115",
            "1100",
            "20240115",
            "1200"
        );
        when(weatherClient.getWeatherForecast(requestTime))
            .thenReturn(new WeatherForecast(21, "1", "0"));

        weatherService.refreshWeather();

        verify(weatherClient).getWeatherForecast(requestTime);
        verify(weatherCacheRepository).save(any(WeatherCache.class));
    }
}
