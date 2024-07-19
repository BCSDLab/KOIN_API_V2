package in.koreatech.koin.domain.bus.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.exception.BusCacheNotFoundException;
import in.koreatech.koin.domain.bus.model.mongo.CityBusTimetable;

public interface CityBusTimetableRepository extends Repository<CityBusTimetable, String> {

    CityBusTimetable save(CityBusTimetable cityBusTimetable);

    Optional<CityBusTimetable> findByBusInfoNumberAndBusInfoArrival(Long number, String arrivalNode);

    default CityBusTimetable getByBusInfoNumberAndBusInfoArrival(Long number, String arrivalNode) {
        return findByBusInfoNumberAndBusInfoArrival(number, arrivalNode)
            .orElseThrow(() -> BusCacheNotFoundException.withDetail("number: " + number + ", direction: " + arrivalNode + " 기점 방향"));
    }
}
