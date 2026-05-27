package in.koreatech.koin.domain.weather.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.weather.client.WeatherClient;
import in.koreatech.koin.domain.weather.dto.WeatherResponse;
import in.koreatech.koin.domain.weather.model.WeatherForecastRequestTime;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final Clock clock;
    private final WeatherClient weatherClient;

    private WeatherResponse cachedWeather;
    private LocalDateTime cachedAt;

    public synchronized WeatherResponse getWeather() {
        LocalDateTime now = LocalDateTime.now(clock);
        if (cachedWeather != null && cachedAt.plus(CACHE_TTL).isAfter(now)) {
            return cachedWeather;
        }

        WeatherForecastRequestTime requestTime = WeatherForecastRequestTime.from(now);
        WeatherResponse response = weatherClient.getWeatherForecast(requestTime).toResponse();
        cachedWeather = response;
        cachedAt = now;
        return response;
    }
}
