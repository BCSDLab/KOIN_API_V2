package in.koreatech.koin.domain.weather.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.weather.client.WeatherClient;
import in.koreatech.koin.domain.weather.client.dto.WeatherForecastRequestTime;
import in.koreatech.koin.domain.weather.dto.WeatherResponse;
import in.koreatech.koin.domain.weather.exception.WeatherOpenApiException;
import in.koreatech.koin.domain.weather.model.WeatherCache;
import in.koreatech.koin.domain.weather.repository.WeatherCacheRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final DateTimeFormatter FORECAST_DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final Clock clock;
    private final WeatherClient weatherClient;
    private final WeatherCacheRepository weatherCacheRepository;

    public WeatherResponse getWeather() {
        String forecastDateTime = LocalDateTime.now(clock)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .format(FORECAST_DATE_TIME_FORMATTER);

        return weatherCacheRepository.findById(WeatherCache.BYEONGCHEON_ID)
            .flatMap(cache -> Optional.ofNullable(cache.getHourlyWeathers()))
            .map(hourlyWeathers -> hourlyWeathers.get(forecastDateTime))
            .orElseThrow(() -> WeatherOpenApiException.withDetail(
                "weather cache is empty, forecastDateTime: " + forecastDateTime
            ));
    }

    public synchronized void refreshWeather() {
        LocalDateTime now = LocalDateTime.now(clock);
        WeatherForecastRequestTime requestTime = WeatherForecastRequestTime.from(now);
        // 외부 API의 시간대별 도메인 예보를 조회 API가 바로 사용할 수 있는 응답 Map으로 한 번에 변환한다.
        Map<String, WeatherResponse> hourlyWeathers = weatherClient.getWeatherForecasts(requestTime).entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().toResponse()
            ));
        weatherCacheRepository.save(WeatherCache.of(hourlyWeathers));
    }
}
