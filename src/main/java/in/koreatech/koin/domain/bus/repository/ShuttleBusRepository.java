package in.koreatech.koin.domain.bus.repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.exception.BusNotFoundException;
import in.koreatech.koin.domain.bus.model.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.model.mongo.ShuttleBusRoute;

public interface ShuttleBusRepository extends Repository<ShuttleBusRoute, ObjectId> {

    List<ShuttleBusRoute> findBySemesterType(String semesterType);

    List<ShuttleBusRoute> findBySemesterTypeAndRegion(String semesterType, ShuttleBusRegion region);

    Optional<ShuttleBusRoute> findById(String id);

    default ShuttleBusRoute getById(String id) {
        return findById(id).orElseThrow(
            () -> BusNotFoundException.withDetail("id: " + id));
    }
}
