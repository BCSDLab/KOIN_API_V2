package in.koreatech.koin.domain.weather.model;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin.domain.weather.dto.WeatherResponse;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("Weather")
public class WeatherCache {

    public static final String BYEONGCHEON_ID = "byeongcheon";
    private static final long CACHE_EXPIRE_HOUR = 1L;

    @Id
    private String id;

    private WeatherForecastRequestTime requestTime;
    private WeatherResponse weather;

    @TimeToLive(unit = TimeUnit.HOURS)
    private final Long expiration;

    @Builder
    private WeatherCache(
        String id,
        WeatherForecastRequestTime requestTime,
        WeatherResponse weather,
        Long expiration
    ) {
        this.id = id;
        this.requestTime = requestTime;
        this.weather = weather;
        this.expiration = expiration == null ? CACHE_EXPIRE_HOUR : expiration;
    }

    public static WeatherCache of(WeatherForecastRequestTime requestTime, WeatherResponse weather) {
        return WeatherCache.builder()
            .id(BYEONGCHEON_ID)
            .requestTime(requestTime)
            .weather(weather)
            .build();
    }
}
