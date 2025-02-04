package in.koreatech.koin.domain.bus.service.route;

import static in.koreatech.koin.domain.bus.model.enums.ShuttleBusRegion.CHEONAN_ASAN;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.model.mongo.ShuttleBusRoute;
import in.koreatech.koin.domain.bus.model.mongo.ShuttleBusRoute.RouteInfo;
import in.koreatech.koin.domain.bus.repository.ShuttleBusRepository;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShuttleBusRouteStrategy implements BusRouteStrategy {

    private final ShuttleBusRepository shuttleBusRepository;
    private final VersionRepository versionRepository;

    @Override
    public List<ScheduleInfo> findSchedule(BusRouteCommand command) {
        Version version = versionRepository.getByTypeAndIsPrevious(VersionType.SHUTTLE, false);
        String semesterType = version.getTitle();
        List<ScheduleInfo> list = new ArrayList<>();
        for (ShuttleBusRoute shuttleBusRoute : shuttleBusRepository.findBySemesterTypeAndRegion(semesterType,
            CHEONAN_ASAN)) {
            if (!shuttleBusRoute.filterDepartAndArriveNode(command.depart(), command.arrive())) {
                continue;
            }
            addSchedule(command, shuttleBusRoute, list);
        }
        return list;
    }

    private void addSchedule(BusRouteCommand command, ShuttleBusRoute shuttleBusRoute, List<ScheduleInfo> list) {
        for (RouteInfo route : shuttleBusRoute.getRouteInfo()) {
            if (!route.filterRoutesByDayOfWeek(command.date())) {
                continue;
            }
            int departNodeIndex = shuttleBusRoute.findArrivalNodeIndexByStation(command.depart());
            ScheduleInfo shuttleBusScheduleInfo = new ScheduleInfo("shuttle",
                shuttleBusRoute.getRouteName(), LocalTime.parse(route.getArrivalTime().get(departNodeIndex)));
            list.add(shuttleBusScheduleInfo);
        }
    }

    @Override
    public boolean support(BusRouteType type) {
        return type == BusRouteType.SHUTTLE || type == BusRouteType.ALL;
    }
}
