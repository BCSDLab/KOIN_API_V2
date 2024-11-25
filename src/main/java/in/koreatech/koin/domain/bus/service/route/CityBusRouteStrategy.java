package in.koreatech.koin.domain.bus.service.route;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.model.city.CityBusRouteType;
import in.koreatech.koin.domain.bus.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.service.CityBusService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CityBusRouteStrategy implements BusRouteStrategy{

    private final CityBusService cityBusService;

    @Override
    public List<ScheduleInfo> findSchedule(BusRouteCommand command) {
        return CityBusRouteType.findRoutes(command.depart(), command.arrive())
            .stream()
            .flatMap(route -> getScheduleForRoute(route, command))
            .toList();
    }

    private Stream<ScheduleInfo> getScheduleForRoute(CityBusRouteType route, BusRouteCommand command) {
        return cityBusService.getCityBusSchedule(
            route.getBusNumber(),
            command.depart(),
            route.getArrivalStation(),
            command.date()
        ).stream();
    }

    @Override
    public boolean support(BusRouteType type) {
        return type == BusRouteType.CITY || type == BusRouteType.ALL;
    }
}
