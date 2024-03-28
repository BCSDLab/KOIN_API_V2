package in.koreatech.koin.domain.bus.util;

import static in.koreatech.koin.domain.bus.model.Constant.CACHE_EXPIRE_MINUTE;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import in.koreatech.koin.domain.bus.model.Bus;
import in.koreatech.koin.domain.bus.model.redis.CityBusCache;
import in.koreatech.koin.domain.version.model.Version;

public abstract class BusOpenApiRequester<T extends Bus> {

    public abstract List<T> getBusRemainTime(String nodeId);

    public boolean isCacheExpired(Version version, Clock clock) {
        Duration duration = Duration.between(version.getUpdatedAt().toLocalTime(), LocalTime.now(clock));

        return 0 <= duration.toSeconds() && duration.toSeconds() < CACHE_EXPIRE_MINUTE * 60;
    }
}
