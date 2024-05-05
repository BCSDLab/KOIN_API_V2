package in.koreatech.koin.domain.coop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coop.model.DiningSoldOutCache;
import in.koreatech.koin.domain.dining.model.DiningType;

public interface DiningSoldOutCacheRepository extends Repository<DiningSoldOutCache, String> {

    DiningSoldOutCache save(DiningSoldOutCache diningSoldOutCache);

    Optional<DiningSoldOutCache> findByDiningType(DiningType diningType);

    default DiningSoldOutCache getByDiningType(DiningType diningType) {
        return findByDiningType(diningType).orElseThrow(() -> new RuntimeException("DiningSoldOutCache not found"));
    }

}
