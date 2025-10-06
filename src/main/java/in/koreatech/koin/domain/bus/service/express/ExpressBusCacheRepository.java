package in.koreatech.koin.domain.bus.service.express;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.exception.BusCacheNotFoundException;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusCache;
import in.koreatech.koin.config.repository.RedisRepositoryMarker;

@RedisRepositoryMarker
public interface ExpressBusCacheRepository extends Repository<ExpressBusCache, String> {

    ExpressBusCache save(ExpressBusCache expressBusCache);

    Optional<ExpressBusCache> findById(String busRoute);

    default ExpressBusCache getById(String busRoute) {
        return findById(busRoute).orElseThrow(() -> BusCacheNotFoundException.withDetail("busRoute: " + busRoute));
    }

    List<ExpressBusCache> findAll();

    boolean existsById(String id);
}
