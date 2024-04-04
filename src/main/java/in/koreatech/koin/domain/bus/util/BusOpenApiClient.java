package in.koreatech.koin.domain.bus.util;

import java.time.Clock;

import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.version.model.Version;

public abstract class BusOpenApiClient<T extends BusRemainTime> {

    public abstract boolean isCacheExpired(Version version, Clock clock);
}
