package in.koreatech.koin.domain.bus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.exception.BusCacheNotFoundException;
import in.koreatech.koin.domain.bus.model.mongo.CityBusTimetable;

public interface CityBusTimetableRepository extends Repository<CityBusTimetable, String> {
    List<CityBusTimetable> findAll();

    Optional<CityBusTimetable> findByRouteId(String routeId);

    Optional<CityBusTimetable> findByBusInfoNumberAndBusInfoArrival(Long number, String arrivalNode);

    default CityBusTimetable getByRouteId(String routeId) {
        return findByRouteId(routeId).orElseThrow(() -> BusCacheNotFoundException.withDetail("routeId: " + routeId));
    }

    default CityBusTimetable getByBusInfoNumberAndBusInfoArrival(Long number, String arrivalNode) {
        return findByBusInfoNumberAndBusInfoArrival(number, arrivalNode)
            .orElseThrow(() -> BusCacheNotFoundException.withDetail("number: " + number + ", direction: " + arrivalNode + " 기점 방향"));
    }
}
