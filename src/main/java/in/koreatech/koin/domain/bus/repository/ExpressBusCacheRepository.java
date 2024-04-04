package in.koreatech.koin.domain.bus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.exception.BusCacheNotFoundException;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCache;

public interface ExpressBusCacheRepository extends Repository<ExpressBusCache, String> {

    ExpressBusCache save(ExpressBusCache expressBusCache);

    Optional<ExpressBusCache> findById(String busRoute);

    default ExpressBusCache getById(String busRoute) {
        return findById(busRoute).orElseThrow(() -> BusCacheNotFoundException.withDetail("busRoute: " + busRoute));
    }

    List<ExpressBusCache> findAll();
}
