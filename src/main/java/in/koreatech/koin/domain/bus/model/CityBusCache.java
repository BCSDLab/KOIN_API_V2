package in.koreatech.koin.domain.bus.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("CityBus")
public class CityBusCache {

    private static final long CACHE_EXPIRE_MINUTE = 1L;

    @Id
    private String id;

    private final List<Long> remainTime = new ArrayList<>();

    @TimeToLive(unit = TimeUnit.MINUTES)
    private final Long expiration;

    @Builder
    private CityBusCache(String id, List<Long> remainTime, Long expiration) {
        this.id = id;
        this.remainTime.addAll(remainTime);
        this.expiration = expiration;
    }

    public static CityBusCache create(String nodeId, List<Long> remainTime) {
        return CityBusCache.builder()
            .id(nodeId)
            .remainTime(remainTime)
            .expiration(CACHE_EXPIRE_MINUTE)
            .build();
    }
}
