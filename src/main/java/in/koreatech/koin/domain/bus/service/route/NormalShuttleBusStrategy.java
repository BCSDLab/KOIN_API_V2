package in.koreatech.koin.domain.bus.service.route;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NormalShuttleBusStrategy implements BusRouteStrategy{

    private final BusRepository busRepository;

    @Override
    public List<ScheduleInfo> findSchedule(BusRouteCommand command) {
        String direction = command.depart().getQueryName() == "한기대" ? "from" : "to";
        return busRepository.getByBusTypeAndDirectionAndRegion("shuttle", direction, "천안").getRoutes()
            .stream()
            .filter(route -> route.filterRoutesByDayOfWeek(command.date()))
            .filter(route -> !route.filterCircularRoutes())
            .filter(route -> route.filterDepartAndArriveNode(command.depart(), command.arrive()))
            .map(route -> route.getShuttleBusScheduleInfo(command.depart()))
            .toList();
    }

    @Override
    public boolean support(BusRouteType type) {
        return type == BusRouteType.SHUTTLE || type == BusRouteType.ALL;
    }
}
