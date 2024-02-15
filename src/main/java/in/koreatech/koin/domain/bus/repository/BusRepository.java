package in.koreatech.koin.domain.bus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import in.koreatech.koin.domain.bus.exception.BusNotFoundException;
import in.koreatech.koin.domain.bus.model.Bus;

public interface BusRepository extends MongoRepository<Bus, String> {

    List<Bus> findAll();

    Optional<Bus> findByBusTypeAndDirection(String busType, String direction);

    default Bus getByBusTypeAndDirection(String busType, String direction) {
        return findByBusTypeAndDirection(busType, direction)
            .orElseThrow(() -> BusNotFoundException.withDetail("busType: " + busType + ", direction: " + direction));
    }
}
