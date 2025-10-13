package in.koreatech.koin.domain.bus.service.express.client;

import java.time.Clock;

import in.koreatech.koin.domain.bus.service.express.ExpressBusCacheRedisRepository;
import in.koreatech.koin.domain.version.repository.VersionRepository;

public abstract class ExpressBusClient {

    protected final ExpressBusCacheRedisRepository expressBusCacheRedisRepository;
    protected final VersionRepository versionRepository;
    protected final Clock clock;

    public ExpressBusClient(
        ExpressBusCacheRedisRepository expressBusCacheRedisRepository,
        VersionRepository versionRepository,
        Clock clock
    ) {
        this.expressBusCacheRedisRepository = expressBusCacheRedisRepository;
        this.versionRepository = versionRepository;
        this.clock = clock;
    }

    public abstract void storeRemainTime();
}
