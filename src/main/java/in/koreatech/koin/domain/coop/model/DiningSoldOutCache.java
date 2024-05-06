package in.koreatech.koin.domain.coop.model;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash(value = "diningSoldOut")
public class DiningSoldOutCache {

    private static final long CACHE_EXPIRE_HOUR = 2L;

    @Id
    private String id;

    @TimeToLive(unit = TimeUnit.HOURS)
    private final Long expiration;

    @Builder
    private DiningSoldOutCache(String id, Long expiration) {
        this.id = id;
        this.expiration = expiration == null ? CACHE_EXPIRE_HOUR : expiration;
    }

    public static DiningSoldOutCache of(String diningType) {
        return DiningSoldOutCache.builder()
            .id(diningType)
            .build();
    }
}
