package in.koreatech.koin.domain.bus.global.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.global.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.global.dto.BusScheduleResponse;
import in.koreatech.koin.domain.bus.global.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.city.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.global.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.shuttle.model.enums.BusStation;
import in.koreatech.koin.domain.bus.global.model.enums.BusType;
import in.koreatech.koin.domain.bus.city.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.shuttle.model.BusCourse;
import in.koreatech.koin.domain.bus.city.model.CityBusTimetable;
import in.koreatech.koin.domain.bus.shuttle.repository.SchoolBusRepository;
import in.koreatech.koin.domain.bus.city.repository.CityBusTimetableRepository;
import in.koreatech.koin.domain.bus.express.repository.ExpressBusCacheRepository;
import in.koreatech.koin.domain.bus.global.service.route.CityBusRouteManager;
import in.koreatech.koin.domain.bus.global.service.route.ExpressBusRouteManager;
import in.koreatech.koin.domain.bus.global.service.route.ShuttleBusRouteManager;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusRouteService {

    private final SchoolBusRepository schoolBusRepository;
    private final CityBusTimetableRepository cityBusTimetableRepository;
    private final ExpressBusCacheRepository expressBusCacheRepository;
    private final List<Long> cityBusList = new ArrayList<>(Arrays.asList(400L, 402L, 405L));

    public BusScheduleResponse getBusSchedule(BusRouteCommand request) {
        List<ScheduleInfo> scheduleInfoList = switch(request.depart()) {
            case KOREATECH -> getBusScheduleDepartFromKoreaTech(request);
            case TERMINAL, STATION -> getBusScheduleDepartFromElse(request, request.depart());
        };
        return new BusScheduleResponse(request.depart(), request.arrive(), request.date(), request.time(), scheduleInfoList);
    }

    private List<ScheduleInfo> getBusScheduleDepartFromKoreaTech(BusRouteCommand request) {
        List<ScheduleInfo> scheduleInfoList = new ArrayList<>();
        scheduleInfoList.addAll(getShuttleBusSchedule(request));
        scheduleInfoList.addAll(getCommutingBusSchedule(request));
        cityBusList.forEach(
            busNumber -> scheduleInfoList.addAll(
                getCityBusSchedule(busNumber, BusStation.KOREATECH, CityBusDirection.종합터미널, request.date())
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

    // 터미널 <-> 천안역 : 구현 X
    private List<ScheduleInfo> getBusScheduleDepartFromElse(BusRouteCommand request, BusStation depart) {
        List<ScheduleInfo> scheduleInfoList = new ArrayList<>();
        BusDirection direction = getRouteDirection(request.depart(), request.arrive());
        scheduleInfoList.addAll(getShuttleBusSchedule(request));
        scheduleInfoList.addAll(getCommutingBusSchedule(request));
        scheduleInfoList.addAll(ExpressBusRouteManager.getExpressBusSchedule(direction));
        scheduleInfoList.addAll(getCityBusSchedule(400L, depart, CityBusDirection.병천3리, request.date()));
        scheduleInfoList.addAll(getCityBusSchedule(402L, depart, CityBusDirection.황사동, request.date()));
        scheduleInfoList.addAll(getCityBusSchedule(405L, depart, CityBusDirection.유관순열사사적지, request.date()));

        return scheduleInfoList.stream()
            .filter(schedule -> schedule.departTime().isAfter(request.time()) &&
                (request.busRouteType() == BusRouteType.ALL || schedule.busType().equals(request.busRouteType().getName())))
            .sorted(Comparator.comparing(ScheduleInfo::departTime))
            .toList();
    }

    private static BusDirection getRouteDirection(BusStation depart, BusStation arrive) {
        if(depart == BusStation.KOREATECH && arrive == BusStation.TERMINAL) return BusDirection.NORTH;
        else return BusDirection.SOUTH;
    }

    private List<ScheduleInfo> getCityBusSchedule(Long busNumber, BusStation depart, CityBusDirection arrival, LocalDate date) {
        CityBusTimetable timetable = cityBusTimetableRepository
            .getByBusInfoNumberAndBusInfoArrival(busNumber, arrival.getName());

        return CityBusRouteManager.getCityBusSchedule(timetable, busNumber, depart, arrival, date);
    }
    private List<ScheduleInfo> getShuttleBusSchedule(BusRouteCommand request) {
        BusType busType = BusType.SHUTTLE;
        BusCourse shuttleFrom = schoolBusRepository.getByBusTypeAndDirectionAndRegion(busType.getName(), "from", "천안");
        BusCourse shuttleTo = schoolBusRepository.getByBusTypeAndDirectionAndRegion(busType.getName(), "to", "천안");
        return ShuttleBusRouteManager.getShuttleBusSchedule(shuttleFrom, shuttleTo, request.depart(), request.arrive(), request.date());
    }

    private List<ScheduleInfo> getCommutingBusSchedule(BusRouteCommand request) {
        BusType busType = BusType.COMMUTING;
        BusCourse shuttleFrom = schoolBusRepository.getByBusTypeAndDirectionAndRegion(busType.getName(), "from", "천안");
        BusCourse shuttleTo = schoolBusRepository.getByBusTypeAndDirectionAndRegion(busType.getName(), "to", "천안");
        return ShuttleBusRouteManager.getCommutingBusSchedule(shuttleFrom, shuttleTo, request.depart(), request.arrive(), request.date());
    }

}
