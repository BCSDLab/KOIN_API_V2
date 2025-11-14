package in.koreatech.koin.admin.bus.shuttle.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;

public interface AdminShuttleBusTimetableRepository extends Repository<ShuttleBusRoute, String> {

    Optional<ShuttleBusRoute> findBySemesterTypeAndRegionAndRouteTypeAndRouteNameAndSubName(
        String semesterType, String region, String routeType, String routeName, String subName
    );

    ShuttleBusRoute save(ShuttleBusRoute shuttleBusRoute);
}
