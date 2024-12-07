package in.koreatech.koin.domain.coop.model;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("DiningNotify")
public class DiningNotifyCache {

    private static final long CACHE_EXPIRE_HOUR_BY_COOP = 3L;

    @Id
    private String id;

    @TimeToLive(unit = TimeUnit.HOURS)
    private final Long expiration;

    @Builder
    private DiningNotifyCache(String id, Long expiration){
        this.id = id;
        this.expiration = CACHE_EXPIRE_HOUR_BY_COOP;
    }

    public static DiningNotifyCache from(String diningId){
        return DiningNotifyCache.builder()
            .id(diningId)
            .build();
    }
}
