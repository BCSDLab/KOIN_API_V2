package in.koreatech.koin.domain.bus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.exception.BusCacheNotFoundException;
import in.koreatech.koin.domain.bus.model.city.CityBusRouteCache;

public interface CityBusRouteCacheRepository extends Repository<CityBusRouteCache, String> {

    CityBusRouteCache save(CityBusRouteCache cityBusRouteCache);

    List<CityBusRouteCache> findAll();

    Optional<CityBusRouteCache> findById(String nodeId);

    default CityBusRouteCache getById(String nodeId) {
        return findById(nodeId).orElseThrow(() -> BusCacheNotFoundException.withDetail("nodeId: " + nodeId));
    }
}
