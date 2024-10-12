package in.koreatech.koin.domain.bus.util.express;

import java.time.Clock;

import in.koreatech.koin.domain.bus.repository.ExpressBusCacheRepository;
import in.koreatech.koin.domain.version.repository.VersionRepository;

public abstract class ExpressBusClient {

    protected final ExpressBusCacheRepository expressBusCacheRepository;
    protected final VersionRepository versionRepository;
    protected final Clock clock;

    public ExpressBusClient(
        ExpressBusCacheRepository expressBusCacheRepository,
        VersionRepository versionRepository,
        Clock clock
    ) {
        this.expressBusCacheRepository = expressBusCacheRepository;
        this.versionRepository = versionRepository;
        this.clock = clock;
    }

    public abstract void storeRemainTime();
}
