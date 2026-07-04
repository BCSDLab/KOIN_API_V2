package in.koreatech.koin.domain.weather.model;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin.domain.weather.dto.WeatherResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("Weather")
public class WeatherCache {

    public static final String BYEONGCHEON_ID = "byeongcheon";
    private static final long CACHE_EXPIRE_HOUR = 24L;

    @Id
    private String id;

    private Map<String, WeatherResponse> hourlyWeathers;

    @TimeToLive(unit = TimeUnit.HOURS)
    private final Long expiration;

    @Builder
    private WeatherCache(
        String id,
        Map<String, WeatherResponse> hourlyWeathers,
        Long expiration
    ) {
        this.id = id;
        this.hourlyWeathers = hourlyWeathers;
        this.expiration = expiration == null ? CACHE_EXPIRE_HOUR : expiration;
    }

    public static WeatherCache of(Map<String, WeatherResponse> hourlyWeathers) {
        return WeatherCache.builder()
            .id(BYEONGCHEON_ID)
            .hourlyWeathers(hourlyWeathers)
            .build();
    }
}
