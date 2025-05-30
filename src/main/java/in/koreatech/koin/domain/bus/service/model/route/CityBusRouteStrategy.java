package in.koreatech.koin.domain.bus.service.model.route;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.enums.BusRouteType;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.service.city.model.CityBusTimetable;
import in.koreatech.koin.domain.bus.service.city.repository.CityBusTimetableRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CityBusRouteStrategy implements BusRouteStrategy {

    private final CityBusTimetableRepository cityBusTimetableRepository;
    private static final Map<Long, CityBusDirection> CITY_BUS_INFO = Map.of(
        400L, CityBusDirection.병천3리,
        402L, CityBusDirection.황사동,
        405L, CityBusDirection.유관순열사사적지
    );

    @Override
    public List<ScheduleInfo> findSchedule(BusRouteCommand command) {
        if (command.depart() == BusStation.STATION && command.arrive() == BusStation.TERMINAL)
            return Collections.emptyList();

        return CITY_BUS_INFO.entrySet().stream()
            .map(entry -> getScheduleForRoute(entry.getKey(), command.depart(), entry.getValue()))
            .flatMap(route -> route.getScheduleInfo(command.date(), command.depart()).stream())
            .toList();
    }

    private CityBusTimetable getScheduleForRoute(Long busNumber, BusStation depart, CityBusDirection cityBusInfo) {
        if (depart == BusStation.TERMINAL) {
            return cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(busNumber, "종합터미널");
        } else {
            return cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(busNumber, cityBusInfo.getName());
        }
    }

    @Override
    public boolean support(BusRouteType type) {
        return type == BusRouteType.CITY || type == BusRouteType.ALL;
    }
}
