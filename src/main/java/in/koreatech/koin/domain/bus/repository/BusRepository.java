package in.koreatech.koin.domain.bus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.exception.BusNotFoundException;
import in.koreatech.koin.domain.bus.model.mongo.BusCourse;

public interface BusRepository extends Repository<BusCourse, String> {

    BusCourse save(BusCourse busCourse);

    List<BusCourse> findAll();

    List<BusCourse> findByBusType(String busType);

    List<BusCourse> findByBusTypeAndRegion(String busType, String region);

    Optional<BusCourse> findByBusTypeAndDirectionAndRegion(String busType, String direction, String region);

    default BusCourse getByBusTypeAndDirectionAndRegion(String busType, String direction, String region) {
        return findByBusTypeAndDirectionAndRegion(busType, direction, region).orElseThrow(
            () -> BusNotFoundException.withDetail("region"));
    }
}
