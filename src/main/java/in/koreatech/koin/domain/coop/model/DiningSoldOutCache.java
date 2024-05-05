package in.koreatech.koin.domain.coop.model;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin.domain.dining.model.DiningType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash(value = "diningSoldOut")
public class DiningSoldOutCache {

    private static final long CACHE_EXPIRE_HOUR = 2L;

    @Id
    private String id;

    private DiningType diningType;

    @TimeToLive(unit = TimeUnit.HOURS)
    private final Long expiration;

    @Builder
    private DiningSoldOutCache(String id, DiningType diningType, Long expiration) {
        this.id = id;
        this.diningType = diningType;
        this.expiration = expiration == null ? CACHE_EXPIRE_HOUR : expiration;
    }

    public static DiningSoldOutCache of(DiningType diningType) {
        return DiningSoldOutCache.builder()
            .id(diningType.name())
            .diningType(diningType)
            .build();
    }

    public static long getCacheExpireHour() {
        return CACHE_EXPIRE_HOUR;
    }
}
