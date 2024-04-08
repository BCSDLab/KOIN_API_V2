package in.koreatech.koin.domain.bus.model.city;

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
    private static final long CACHE_EXPIRE_SECONDS = 60L;

    @Id
    private String id;

    private final List<CityBusCacheInfo> busInfos = new ArrayList<>();

    @TimeToLive(unit = TimeUnit.MINUTES)
    private final Long expiration;

    @Builder
    private CityBusCache(String id, List<CityBusCacheInfo> busInfos, Long expiration) {
        this.id = id;
        this.busInfos.addAll(busInfos);
        this.expiration = expiration;
    }

    public static CityBusCache of(String nodeId, List<CityBusCacheInfo> busInfos) {
        return CityBusCache.builder()
            .id(nodeId)
            .busInfos(busInfos)
            .expiration(CACHE_EXPIRE_MINUTE)
            .build();
    }

    public static long getCacheExpireSeconds() {
        return CACHE_EXPIRE_SECONDS;
    }
}
