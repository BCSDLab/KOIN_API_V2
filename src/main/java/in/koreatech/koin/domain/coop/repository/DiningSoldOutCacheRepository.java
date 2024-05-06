package in.koreatech.koin.domain.coop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coop.exception.DiningCacheNotFoundException;
import in.koreatech.koin.domain.coop.model.DiningSoldOutCache;

public interface DiningSoldOutCacheRepository extends Repository<DiningSoldOutCache, String> {

    DiningSoldOutCache save(DiningSoldOutCache diningSoldOutCache);

    Optional<DiningSoldOutCache> findById(String diningType);

    default DiningSoldOutCache getById(String diningType) {
        return findById(diningType).orElseThrow(
            () -> DiningCacheNotFoundException.withDetail("diningSoldOutCache: " + diningType));
    }

}
