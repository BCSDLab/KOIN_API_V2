package in.koreatech.koin.domain.bus.model.redis;

import static in.koreatech.koin.domain.bus.model.Constant.CACHE_EXPIRE_MINUTE;

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

    @Id
    private String id;

    private final List<BusCache> busInfos = new ArrayList<>();

    @TimeToLive(unit = TimeUnit.MINUTES)
    private final Long expiration;

    @Builder
    private CityBusCache(String id, List<BusCache> busInfos, Long expiration) {
        this.id = id;
        this.busInfos.addAll(busInfos);
        this.expiration = expiration;
    }

    public static CityBusCache create(String nodeId, List<BusCache> busInfos) {
        return CityBusCache.builder()
            .id(nodeId)
            .busInfos(busInfos)
            .expiration(CACHE_EXPIRE_MINUTE)
            .build();
    }
}
