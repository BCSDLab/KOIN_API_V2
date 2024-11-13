package in.koreatech.koin.domain.bus.city.model;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("CityBusRoute")
public class CityBusRouteCache {

    public static final long CACHE_EXPIRE_MINUTE = 2L;

    @Id
    private final String id;

    private final Set<Long> busNumbers = new HashSet<>();

    @TimeToLive(unit = TimeUnit.MINUTES)
    private final Long expiration;

    @Builder
    private CityBusRouteCache(String id, Set<Long> busNumbers, Long expiration) {
        this.id = id;
        this.busNumbers.addAll(busNumbers);
        this.expiration = expiration;
    }
}
