package in.koreatech.koin.domain.bus.facade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.service.CityBusService;
import in.koreatech.koin.domain.bus.service.ShuttleBusService;
import in.koreatech.koin.domain.bus.service.route.ExpressBusRouteManager;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusRouteFacade {

    private static final Map<Long, CityBusDirection> CITY_BUS_INFO = Map.of(
        400L, CityBusDirection.병천3리,
        402L, CityBusDirection.황사동,
        405L, CityBusDirection.유관순열사사적지
    );
    private final ShuttleBusService shuttleBusService;
    private final CityBusService cityBusService;

    public BusScheduleResponse getBusSchedule(BusRouteCommand request) {
        List<ScheduleInfo> scheduleInfoList = switch (request.depart()) {
            case KOREATECH -> getBusScheduleDepartFromKoreaTech(request);
            case TERMINAL, STATION -> getBusScheduleDepartFromElse(request);
        };
        return new BusScheduleResponse(
            request.depart(), request.arrive(), request.date(), request.time(),
            filterAndSortSchedules(scheduleInfoList, request)
        );
    }

    private List<ScheduleInfo> getBusScheduleDepartFromKoreaTech(BusRouteCommand request) {
        List<ScheduleInfo> scheduleInfoList = new ArrayList<>();

        if (request.arrive() == BusStation.TERMINAL) {
            addExpressBusSchedules(scheduleInfoList, request);
        }

        addShuttleBusSchedules(scheduleInfoList, request);
        addCityBusSchedules(scheduleInfoList, request);

        return scheduleInfoList;
    }

    private List<ScheduleInfo> getBusScheduleDepartFromElse(BusRouteCommand request) {
        if (request.arrive() == BusStation.STATION || request.arrive() == BusStation.TERMINAL) {
            return Collections.emptyList();
        }
        List<ScheduleInfo> scheduleInfoList = new ArrayList<>();

        if (request.depart() == BusStation.TERMINAL) {
            addExpressBusSchedules(scheduleInfoList, request);
        }

        addShuttleBusSchedules(scheduleInfoList, request);
        addCityBusSchedules(scheduleInfoList, request);

        return scheduleInfoList;
    }

    private void addShuttleBusSchedules(List<ScheduleInfo> scheduleInfoList, BusRouteCommand request) {
        scheduleInfoList.addAll(shuttleBusService.getShuttleBusSchedule(request, BusType.SHUTTLE));
        scheduleInfoList.addAll(shuttleBusService.getShuttleBusSchedule(request, BusType.COMMUTING));
    }

    private void addCityBusSchedules(List<ScheduleInfo> scheduleInfoList, BusRouteCommand request) {
        if (request.depart() == BusStation.KOREATECH) {
            CITY_BUS_INFO.keySet().forEach(busNumber -> scheduleInfoList.addAll(
                cityBusService.getCityBusSchedule(busNumber, BusStation.KOREATECH, CityBusDirection.종합터미널,
                    request.date())
            ));
        } else {
            CITY_BUS_INFO.forEach((busNumber, arrival) -> scheduleInfoList.addAll(
                cityBusService.getCityBusSchedule(busNumber, request.depart(), arrival,
                    request.date())
            ));
        }
    }

    private void addExpressBusSchedules(List<ScheduleInfo> scheduleInfoList, BusRouteCommand request) {
        BusDirection direction = getRouteDirection(request.depart(), request.arrive());
        scheduleInfoList.addAll(ExpressBusRouteManager.getExpressBusSchedule(direction));
    }

    private List<ScheduleInfo> filterAndSortSchedules(List<ScheduleInfo> scheduleInfoList, BusRouteCommand request) {
        return scheduleInfoList.stream()
            .filter(schedule -> schedule.departTime().isAfter(request.time()) &&
                (request.busRouteType() == BusRouteType.ALL || schedule.busType()
                    .equals(request.busRouteType().getName())))
            .sorted(Comparator.comparing(ScheduleInfo::departTime))
            .toList();
    }

    private BusDirection getRouteDirection(BusStation depart, BusStation arrive) {
        return (depart == BusStation.KOREATECH && arrive == BusStation.TERMINAL)
            ? BusDirection.NORTH : BusDirection.SOUTH;
    }
}
