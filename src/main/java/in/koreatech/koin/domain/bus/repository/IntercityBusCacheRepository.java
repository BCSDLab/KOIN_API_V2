package in.koreatech.koin.domain.bus.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.exception.BusCacheNotFoundException;
import in.koreatech.koin.domain.bus.model.redis.IntercityBusCache;

public interface IntercityBusCacheRepository extends Repository <IntercityBusCache, String>{

    IntercityBusCache save(IntercityBusCache intercityBusCache);

    Optional<IntercityBusCache> findById(String busRoute);

    default IntercityBusCache getById(String busRoute){
        return findById(busRoute).orElseThrow(() -> BusCacheNotFoundException.withDetail("busRoute: " + busRoute));
    }
}
