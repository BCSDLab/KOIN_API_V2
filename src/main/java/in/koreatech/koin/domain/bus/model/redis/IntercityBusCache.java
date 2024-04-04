package in.koreatech.koin.domain.bus.model.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin.domain.bus.model.IntercityBusRoute;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("intercityBus")
public class IntercityBusCache {

    private static final long CACHE_EXPIRE_HOUR = 1L;

    @Id
    private String id;

    private final List<IntercityBusCacheInfo> busInfos = new ArrayList<>();

    @TimeToLive(unit = TimeUnit.HOURS)
    private final Long expiration;

    @Builder
    private IntercityBusCache(String id, List<IntercityBusCacheInfo> busInfos, Long expiration){
        this.id = id;
        this.busInfos.addAll(busInfos);
        this.expiration = expiration;
    }

    public static IntercityBusCache create(IntercityBusRoute route, List<IntercityBusCacheInfo> busInfos){
        return IntercityBusCache.builder()
            .id(route.depTerminalId() + route.arrTerminalId())
            .busInfos(busInfos)
            .expiration(CACHE_EXPIRE_HOUR)
            .build();
    }

    public static long getCacheExpireHour() {
        return CACHE_EXPIRE_HOUR;
    }
}
