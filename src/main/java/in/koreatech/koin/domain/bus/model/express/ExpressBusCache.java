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

    private static final long CACHE_EXPIRE_HOUR = 48L;

    @Id
    private final String id;

    private final List<ExpressBusCacheInfo> busInfos;

    @TimeToLive(unit = TimeUnit.HOURS)
    private final Long expiration;

    @Builder
    private ExpressBusCache(String id, List<ExpressBusCacheInfo> busInfos, Long expiration) {
        this.id = id;
        this.busInfos = (busInfos == null) ? new ArrayList<>() : busInfos;
        this.expiration = expiration == null ? CACHE_EXPIRE_HOUR : expiration;
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
