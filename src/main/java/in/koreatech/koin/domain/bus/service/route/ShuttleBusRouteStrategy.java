package in.koreatech.koin.domain.bus.service.route;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.model.mongo.BusCourse;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShuttleBusRouteStrategy implements BusRouteStrategy{
    private final BusRepository busRepository;
    private static final String BUS_TYPE = "shuttle";
    private static final String REGION = "천안";

    @Override
    public List<ScheduleInfo> findSchedule(BusRouteCommand command) {
        return busRepository.findByBusTypeAndRegion(BUS_TYPE, REGION).stream()
            .map(BusCourse::getRoutes)
            .flatMap(routes ->
                routes.stream()
                    .filter(route -> route.filterRoutesByDayOfWeek(command.date()))
                    .filter(route -> route.filterDepartAndArriveNode(command.depart(), command.arrive()))
                    .map(route -> route.getShuttleBusScheduleInfo(command.depart()))
            )
            .distinct()
            .toList();
    }

    @Override
    public boolean support(BusRouteType type) {
        return type == BusRouteType.SHUTTLE || type == BusRouteType.ALL;
    }
}
