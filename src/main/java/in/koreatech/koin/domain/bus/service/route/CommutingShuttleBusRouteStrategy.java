package in.koreatech.koin.domain.bus.service.route;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.model.mongo.BusCourse;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommutingShuttleBusRouteStrategy implements BusRouteStrategy{

    private final BusRepository busRepository;

    @Override
    public List<ScheduleInfo> findSchedule(BusRouteCommand command) {
        return busRepository.findByBusTypeAndRegion("commuting", "천안").stream()
            .map(BusCourse::getRoutes)
            .flatMap(routes ->
                routes.stream()
                    .filter(route -> route.filterRoutesByDayOfWeek(command.date()))
                    .filter(route -> route.filterDepartAndArriveNode(command.depart(), command.arrive()))
                    .map(route -> route.getCommutingShuttleBusScheduleInfo(command.depart()))
            )
            .distinct()
            .toList();
    }

    @Override
    public boolean support(BusRouteType type) {
        return type == BusRouteType.SHUTTLE || type == BusRouteType.ALL;
    }
}
