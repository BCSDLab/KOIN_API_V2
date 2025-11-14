package in.koreatech.koin.admin.bus.commuting.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;

public interface AdminCommutingBusRepository extends Repository<ShuttleBusRoute, String> {

    @Query("{ 'semester_type': ?0, 'region': ?1, 'route_type': ?2, 'route_name': ?3, 'sub_name': ?4 }")
    Optional<ShuttleBusRoute> findBySemesterTypeAndRegionAndRouteTypeAndRouteNameAndSubName(
        String semesterType,
        ShuttleBusRegion region,
        ShuttleRouteType routeType,
        String routeName,
        String subName
    );

    void save(ShuttleBusRoute shuttleBusRoute);
}
