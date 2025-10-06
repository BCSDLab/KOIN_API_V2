package in.koreatech.koin.domain.coop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coop.exception.DiningCacheNotFoundException;
import in.koreatech.koin.domain.coop.model.DiningNotifyCache;
import in.koreatech.koin.config.repository.RedisRepositoryMarker;

@RedisRepositoryMarker
public interface DiningNotifyCacheRepository extends Repository<DiningNotifyCache, String> {

    DiningNotifyCache save(DiningNotifyCache diningNotifyCache);

    boolean existsById(String diningNotifyId);

    Optional<DiningNotifyCache> findById(String diningPlace);

    default DiningNotifyCache getById(String diningPlace) {
        return findById(diningPlace).orElseThrow(
            () -> DiningCacheNotFoundException.withDetail("diningSoldOutCache: " + diningPlace));
    }
}
