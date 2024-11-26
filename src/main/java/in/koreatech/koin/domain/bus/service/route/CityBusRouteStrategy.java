package in.koreatech.koin.domain.bus.service.route;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.model.mongo.CityBusTimetable;
import in.koreatech.koin.domain.bus.repository.CityBusTimetableRepository;
import lombok.RequiredArgsConstructor;

@Service
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
