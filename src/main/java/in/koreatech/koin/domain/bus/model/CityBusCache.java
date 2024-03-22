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

    private final List<CityBusArrivalInfo> busArrivalInfos = new ArrayList<>();

    @TimeToLive(unit = TimeUnit.MINUTES)
    private final Long expiration;

    @Builder
    private CityBusCache(String id, List<CityBusArrivalInfo> busArrivalInfos, Long expiration) {
        this.id = id;
        this.busArrivalInfos.addAll(busArrivalInfos);
        this.expiration = expiration;
    }

    public static CityBusCache create(String nodeId, List<CityBusArrivalInfo> busArrivalInfos) {
        return CityBusCache.builder()
            .id(nodeId)
            .busArrivalInfos(busArrivalInfos)
            .expiration(CACHE_EXPIRE_MINUTE)
            .build();
    }
}
