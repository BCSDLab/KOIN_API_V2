package in.koreatech.koin.domain.bus.model.express;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash(value = "expressBus")
public class ExpressBusCache {

    private static final long CACHE_EXPIRE_HOUR = 1L;

    @Id
    private String id;

    private final List<ExpressBusCacheInfo> busInfos = new ArrayList<>();

    @TimeToLive(unit = TimeUnit.MINUTES)
    private final Long expiration;

    @Builder
    private ExpressBusCache(String id, List<ExpressBusCacheInfo> busInfos) {
        this.id = id;
        this.busInfos.addAll(busInfos);
        this.expiration = CACHE_EXPIRE_HOUR;
    }

    public static ExpressBusCache of(ExpressBusRoute route, List<ExpressBusCacheInfo> busInfos) {
        return ExpressBusCache.builder()
            .id(generateId(route))
            .busInfos(busInfos)
            .build();
    }

    public static long getCacheExpireHour() {
        return CACHE_EXPIRE_HOUR;
    }

    public static String generateId(ExpressBusRoute route) {
        return String.format("%s:%s", route.depTerminalName(), route.arrTerminalName());
    }
}
