package in.koreatech.koin.domain.bus.service.city.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.exception.BusCacheNotFoundException;
import in.koreatech.koin.domain.bus.service.city.model.CityBusTimetable;
import in.koreatech.koin.global.config.repository.MongoRepository;

@MongoRepository
public interface CityBusTimetableRepository extends Repository<CityBusTimetable, String> {

    CityBusTimetable save(CityBusTimetable cityBusTimetable);

    Optional<CityBusTimetable> findByBusInfoNumberAndBusInfoArrival(Long number, String arrivalNode);

    default CityBusTimetable getByBusInfoNumberAndBusInfoArrival(Long number, String arrivalNode) {
        return findByBusInfoNumberAndBusInfoArrival(number, arrivalNode)
            .orElseThrow(() -> BusCacheNotFoundException.withDetail(
                "number: " + number + ", direction: " + arrivalNode + " 기점 방향"));
    }

    Optional<CityBusTimetable> findByBusInfoNumberAndBusInfoDepart(Long number, String departNode);

    default CityBusTimetable getByBusInfoNumberAndBusInfoDepart(Long number, String departNode) {
        return findByBusInfoNumberAndBusInfoDepart(number, departNode)
            .orElseThrow(() -> BusCacheNotFoundException.withDetail(
                "number: " + number + ", direction: " + departNode + " 종점 방향"));
    }
}
