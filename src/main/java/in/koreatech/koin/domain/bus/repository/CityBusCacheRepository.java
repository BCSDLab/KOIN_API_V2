package in.koreatech.koin.domain.bus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.exception.BusCacheNotFoundException;
import in.koreatech.koin.domain.bus.model.CityBusCache;

public interface CityBusCacheRepository extends Repository<CityBusCache, String> {

    CityBusCache save(CityBusCache cityBusCache);

    List<CityBusCache> findAll();

    Optional<CityBusCache> findById(String nodeId);

    default CityBusCache getById(String nodeId) {
        return findById(nodeId).orElseThrow(() -> BusCacheNotFoundException.withDetail("nodeId: " + nodeId));
    }
}
