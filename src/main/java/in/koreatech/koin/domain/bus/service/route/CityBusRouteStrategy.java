package in.koreatech.koin.domain.bus.service.route;

import static in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo.toScheduleInfo;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.model.city.CityBusRouteType;
import in.koreatech.koin.domain.bus.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.model.mongo.CityBusTimetable;
import in.koreatech.koin.domain.bus.repository.CityBusTimetableRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CityBusRouteStrategy implements BusRouteStrategy{

    private final CityBusTimetableRepository cityBusTimetableRepository;

    @Override
    public List<ScheduleInfo> findSchedule(BusRouteCommand command) {
        return CityBusRouteType.findRoutes(command.depart(), command.arrive())
            .stream()
            .flatMap(route -> getScheduleForRoute(route, command))
            .toList();
    }

    private Stream<ScheduleInfo> getScheduleForRoute(CityBusRouteType route, BusRouteCommand command) {
        CityBusTimetable cityBusSchedule = cityBusTimetableRepository
            .getByBusInfoNumberAndBusInfoArrival(route.getBusNumber(), route.getArrivalStation().getName());

        cityBusSchedule.filterBusTimeTablesByDayOfWeek(command.date());

        // 시내버스 도착 시간 보정(기점 ~ 한기대 정류장 까지의 시간 추가)
        List<LocalTime> adjustedTimetable = cityBusSchedule.getBusTimetables()
            .stream()
            .findFirst()
            .get()
            .applyTimeOffset(route.getBusNumber(), route.getArrivalStation(), command.depart());

        return toScheduleInfo(adjustedTimetable, "city", route.getBusNumber().toString()).stream();
    }

    @Override
    public boolean support(BusRouteType type) {
        return type == BusRouteType.CITY || type == BusRouteType.ALL;
    }
}
