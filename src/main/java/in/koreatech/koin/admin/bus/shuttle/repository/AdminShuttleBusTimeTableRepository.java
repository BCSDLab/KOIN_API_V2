package in.koreatech.koin.admin.bus.shuttle.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimeTable;

public interface AdminShuttleBusTimeTableRepository extends Repository<ShuttleBusTimeTable, String> {

    Optional<ShuttleBusTimeTable> findBySemesterTypeAndRegionAndRouteTypeAndRouteNameAndSubName(
        String semesterType, String region, String routeType, String routeName, String subName
    );

    ShuttleBusTimeTable save(ShuttleBusTimeTable shuttleBusTimeTable);
}
