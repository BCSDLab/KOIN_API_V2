package in.koreatech.koin.domain.bus.city.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.global.exception.BusCacheNotFoundException;
import in.koreatech.koin.domain.bus.city.model.CityBusCache;

public interface CityBusCacheRepository extends Repository<CityBusCache, String> {

    CityBusCache save(CityBusCache cityBusCache);

    List<CityBusCache> findAll();

    Optional<CityBusCache> findById(String nodeId);

    default CityBusCache getById(String nodeId) {
        return findById(nodeId).orElseThrow(() -> BusCacheNotFoundException.withDetail("nodeId: " + nodeId));
    }
}
