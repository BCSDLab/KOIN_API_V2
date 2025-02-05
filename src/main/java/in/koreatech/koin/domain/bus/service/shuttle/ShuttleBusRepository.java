package in.koreatech.koin.domain.bus.service.shuttle;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.exception.BusNotFoundException;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;

public interface ShuttleBusRepository extends Repository<ShuttleBusRoute, ObjectId> {

    List<ShuttleBusRoute> findBySemesterType(String semesterType);

    List<ShuttleBusRoute> findBySemesterTypeAndRegion(String semesterType, ShuttleBusRegion region);

    List<ShuttleBusRoute> findAllByRegionAndRouteTypeAndSemesterType(ShuttleBusRegion region,
        ShuttleRouteType routeType, String semesterType);

    List<ShuttleBusRoute> findAllByBusType(ShuttleRouteType busType);

    Optional<ShuttleBusRoute> findById(String id);

    default ShuttleBusRoute getById(String id) {
        return findById(id).orElseThrow(
            () -> BusNotFoundException.withDetail("id: " + id));
    }
}
