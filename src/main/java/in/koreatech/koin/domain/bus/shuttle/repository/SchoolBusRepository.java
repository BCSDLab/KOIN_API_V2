package in.koreatech.koin.domain.bus.shuttle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.global.exception.BusNotFoundException;
import in.koreatech.koin.domain.bus.shuttle.model.BusCourse;

public interface SchoolBusRepository extends Repository<BusCourse, String> {

    BusCourse save(BusCourse busCourse);

    List<BusCourse> findAll();

    List<BusCourse> findByBusType(String busType);

    Optional<BusCourse> findByBusTypeAndDirectionAndRegion(String busType, String direction, String region);

    default BusCourse getByBusTypeAndDirectionAndRegion(String busType, String direction, String region) {
        return findByBusTypeAndDirectionAndRegion(busType, direction, region).orElseThrow(
            () -> BusNotFoundException.withDetail("region"));
    }
}
