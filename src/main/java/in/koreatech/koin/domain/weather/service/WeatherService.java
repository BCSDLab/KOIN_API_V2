package in.koreatech.koin.domain.weather.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.weather.client.WeatherClient;
import in.koreatech.koin.domain.weather.dto.WeatherResponse;
import in.koreatech.koin.domain.weather.exception.WeatherOpenApiException;
import in.koreatech.koin.domain.weather.model.WeatherCache;
import in.koreatech.koin.domain.weather.model.WeatherForecastRequestTime;
import in.koreatech.koin.domain.weather.repository.WeatherCacheRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final Clock clock;
    private final WeatherClient weatherClient;
    private final WeatherCacheRepository weatherCacheRepository;

    public WeatherResponse getWeather() {
        return weatherCacheRepository.findById(WeatherCache.BYEONGCHEON_ID)
            .map(WeatherCache::getWeather)
            .orElseThrow(() -> WeatherOpenApiException.withDetail("weather cache is empty"));
    }

    public synchronized void refreshWeather() {
        LocalDateTime now = LocalDateTime.now(clock);
        WeatherForecastRequestTime requestTime = WeatherForecastRequestTime.from(now);
        WeatherResponse response = weatherClient.getWeatherForecast(requestTime).toResponse();
        weatherCacheRepository.save(WeatherCache.of(requestTime, response));
    }
}
