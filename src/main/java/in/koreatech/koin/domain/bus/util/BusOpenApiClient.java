package in.koreatech.koin.domain.bus.util;

import static in.koreatech.koin.domain.bus.model.Constant.CACHE_EXPIRE_MINUTE;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.version.model.Version;

public abstract class BusOpenApiClient<T extends BusRemainTime> {

    public abstract List<T> getBusRemainTime(String nodeId);

    public boolean isCacheExpired(Version version, Clock clock) {
        Duration duration = Duration.between(version.getUpdatedAt().toLocalTime(), LocalTime.now(clock));

        return duration.toSeconds() < 0 || CACHE_EXPIRE_MINUTE * 60 <= duration.toSeconds();
    }
}
