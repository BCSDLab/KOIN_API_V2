package in.koreatech.koin.domain.bus.global.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.city.service.CityBusService;
import in.koreatech.koin.domain.bus.global.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.global.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.city.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.global.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.shuttle.model.enums.BusStation;
import in.koreatech.koin.domain.bus.global.model.enums.BusType;
import in.koreatech.koin.domain.bus.city.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.global.service.route.ExpressBusRouteManager;
import in.koreatech.koin.domain.bus.shuttle.service.ShuttleBusService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusRouteService {

    private final ShuttleBusService shuttleBusService;
    private final CityBusService cityBusService;
    private final List<Long> cityBusList = new ArrayList<>(Arrays.asList(400L, 402L, 405L));

    public List<ScheduleInfo> getBusScheduleDepartFromKoreaTech(BusRouteCommand request) {
        List<ScheduleInfo> scheduleInfoList = new ArrayList<>();
        scheduleInfoList.addAll(shuttleBusService.getShuttleBusSchedule(request, BusType.SHUTTLE));
        scheduleInfoList.addAll(shuttleBusService.getShuttleBusSchedule(request, BusType.COMMUTING));
        cityBusList.forEach(
            busNumber -> scheduleInfoList.addAll(
                cityBusService.getCityBusSchedule(busNumber, BusStation.KOREATECH, CityBusDirection.종합터미널, request.date())
            )
        );

        if(request.arrive() == BusStation.TERMINAL) {
            BusDirection direction = getRouteDirection(request.depart(), request.arrive());
            scheduleInfoList.addAll(ExpressBusRouteManager.getExpressBusSchedule(direction));
        }

        return scheduleInfoList.stream()
            .filter(schedule -> schedule.departTime().isAfter(request.time()) &&
                (request.busRouteType() == BusRouteType.ALL || schedule.busType().equals(request.busRouteType().getName())))
            .sorted(Comparator.comparing(ScheduleInfo::departTime))
            .toList();
    }

    public List<ScheduleInfo> getBusScheduleDepartFromElse(BusRouteCommand request, BusStation depart) {
        if (request.arrive() == BusStation.STATION || request.arrive() == BusStation.TERMINAL) {
            return Collections.emptyList();
        }
        List<ScheduleInfo> scheduleInfoList = new ArrayList<>();
        BusDirection direction = getRouteDirection(request.depart(), request.arrive());

        if(depart == BusStation.TERMINAL) {
            scheduleInfoList.addAll(ExpressBusRouteManager.getExpressBusSchedule(direction));
        }
        scheduleInfoList.addAll(shuttleBusService.getShuttleBusSchedule(request, BusType.SHUTTLE));
        scheduleInfoList.addAll(shuttleBusService.getShuttleBusSchedule(request, BusType.COMMUTING));
        scheduleInfoList.addAll(cityBusService.getCityBusSchedule(400L, depart, CityBusDirection.병천3리, request.date()));
        scheduleInfoList.addAll(cityBusService.getCityBusSchedule(402L, depart, CityBusDirection.황사동, request.date()));
        scheduleInfoList.addAll(cityBusService.getCityBusSchedule(405L, depart, CityBusDirection.유관순열사사적지, request.date()));

        return scheduleInfoList.stream()
            .filter(schedule -> schedule.departTime().isAfter(request.time()) &&
                (request.busRouteType() == BusRouteType.ALL || schedule.busType().equals(request.busRouteType().getName())))
            .sorted(Comparator.comparing(ScheduleInfo::departTime))
            .toList();
    }

    private static BusDirection getRouteDirection(BusStation depart, BusStation arrive) {
        if(depart == BusStation.KOREATECH && arrive == BusStation.TERMINAL) {
            return BusDirection.NORTH;
        } else {
            return BusDirection.SOUTH;
        }
    }
}
