package in.koreatech.koin.domain.bus.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


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
import in.koreatech.koin.domain.bus.model.mongo.BusCourse;
import in.koreatech.koin.domain.bus.model.mongo.CityBusTimetable;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import in.koreatech.koin.domain.bus.repository.CityBusTimetableRepository;
import in.koreatech.koin.domain.bus.repository.ExpressBusCacheRepository;
import in.koreatech.koin.domain.bus.service.route.CityBusRouteManager;
import in.koreatech.koin.domain.bus.service.route.ExpressBusRouteManager;
import in.koreatech.koin.domain.bus.service.route.ShuttleBusRouteManager;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusRouteService {

    private final BusRepository busRepository;
    private final CityBusTimetableRepository cityBusTimetableRepository;
    private final ExpressBusCacheRepository expressBusCacheRepository;
    private final List<Long> cityBusList = new ArrayList<>(Arrays.asList(400L, 402L, 405L));

    public BusScheduleResponse getBusSchedule(BusRouteCommand request) {
        // BusDirection direction = getRouteDirection(depart, arrive);
        //
        // List<ScheduleInfo> expressSchedule = ExpressBusRouteManager.getExpressBusSchedule(direction);

        List<ScheduleInfo> scheduleInfoList = switch(request.depart()) {
            case KOREATECH -> getBusScheduleDepartFromKoreaTech(request);
            case TERMINAL, STATION -> getBusScheduleDepartFromElse(request, request.depart());
        };

        // if (depart == BusStation.KOREATECH && arrive == BusStation.TERMINAL) {
        //     scheduleInfoList.addAll(expressSchedule);
        //     scheduleInfoList.addAll(getShuttleBusSchedule(depart, arrive, date));
        //     scheduleInfoList.addAll(getCommutingBusSchedule(depart, arrive, date));
        //     scheduleInfoList.addAll(getCityBusSchedule(400L, BusStation.KOREATECH, CityBusDirection.종합터미널, date));
        //     scheduleInfoList.addAll(getCityBusSchedule(402L, BusStation.KOREATECH, CityBusDirection.종합터미널, date));
        //     scheduleInfoList.addAll(getCityBusSchedule(405L, BusStation.KOREATECH, CityBusDirection.종합터미널, date));
        // }
        //
        // if(depart == BusStation.KOREATECH && arrive == BusStation.STATION) {
        //     scheduleInfoList.addAll(getShuttleBusSchedule(depart, arrive, date));
        //     scheduleInfoList.addAll(getCommutingBusSchedule(depart, arrive, date));
        //     scheduleInfoList.addAll(getCityBusSchedule(400L, BusStation.KOREATECH, CityBusDirection.종합터미널, date));
        //     scheduleInfoList.addAll(getCityBusSchedule(402L, BusStation.KOREATECH, CityBusDirection.종합터미널, date));
        //     scheduleInfoList.addAll(getCityBusSchedule(405L, BusStation.KOREATECH, CityBusDirection.종합터미널, date));
        // }
        //
        // if(depart == BusStation.TERMINAL && arrive == BusStation.KOREATECH) {
        //     scheduleInfoList.addAll(expressSchedule);
        //     scheduleInfoList.addAll(getShuttleBusSchedule(depart, arrive, date));
        //     scheduleInfoList.addAll(getCommutingBusSchedule(depart, arrive, date));
        //     scheduleInfoList.addAll(getCityBusSchedule(400L, BusStation.TERMINAL, CityBusDirection.병천3리, date));
        //     scheduleInfoList.addAll(getCityBusSchedule(402L, BusStation.TERMINAL, CityBusDirection.황사동, date));
        //     scheduleInfoList.addAll(getCityBusSchedule(405L, BusStation.TERMINAL, CityBusDirection.유관순열사사적지, date));
        // }
        //
        // if(depart == BusStation.STATION && arrive == BusStation.KOREATECH) {
        //     scheduleInfoList.addAll(getShuttleBusSchedule(depart, arrive, date));
        //     scheduleInfoList.addAll(getCommutingBusSchedule(depart, arrive, date));
        //     scheduleInfoList.addAll(getCityBusSchedule(400L, BusStation.STATION, CityBusDirection.병천3리, date));
        //     scheduleInfoList.addAll(getCityBusSchedule(402L, BusStation.STATION, CityBusDirection.황사동, date));
        //     scheduleInfoList.addAll(getCityBusSchedule(405L, BusStation.STATION, CityBusDirection.유관순열사사적지, date));
        // }
        //
        // if(depart == BusStation.STATION && arrive == BusStation.TERMINAL) {
        //     // 힣
        // }
        //
        // if(depart == BusStation.TERMINAL && arrive == BusStation.STATION) {
        //     // 헿
        // }
        //
        //
        // List<ScheduleInfo> collect = scheduleInfoList.stream()
        //     .sorted(Comparator.comparing(ScheduleInfo::departTime))
        //     .filter(schedule -> schedule.departTime().isAfter(time))
        //     .toList();
        //
        // if (busRouteType != BusRouteType.ALL) {
        //     collect = collect.stream()
        //         .filter(scheduleInfo -> scheduleInfo.busType().equals(busRouteType.getName()))
        //         .toList();
        // }

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

        // switch(arrive) {
        //     case TERMINAL -> {
        //         cityBusList.forEach(
        //                 busNumber -> scheduleInfoList.addAll(
        //                     getCityBusSchedule(busNumber, BusStation.KOREATECH, CityBusDirection.종합터미널, date)
        //                 )
        //         );
        //     }
        //     case STATION ->  {
        //         cityBusList.forEach(
        //             busNumber -> scheduleInfoList.addAll(
        //                 getCityBusSchedule(busNumber, BusStation.KOREATECH, CityBusDirection.종합터미널, date)
        //             )
        //         );
        //     }
        //     default -> {
        //
        //     }
        // }
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
        BusCourse shuttleFrom = busRepository.getByBusTypeAndDirectionAndRegion(busType.getName(), "from", "천안");
        BusCourse shuttleTo = busRepository.getByBusTypeAndDirectionAndRegion(busType.getName(), "to", "천안");
        return ShuttleBusRouteManager.getShuttleBusSchedule(shuttleFrom, shuttleTo, request.depart(), request.arrive(), request.date());
    }

    private List<ScheduleInfo> getCommutingBusSchedule(BusRouteCommand request) {
        BusType busType = BusType.COMMUTING;
        BusCourse shuttleFrom = busRepository.getByBusTypeAndDirectionAndRegion(busType.getName(), "from", "천안");
        BusCourse shuttleTo = busRepository.getByBusTypeAndDirectionAndRegion(busType.getName(), "to", "천안");
        return ShuttleBusRouteManager.getCommutingBusSchedule(shuttleFrom, shuttleTo, request.depart(), request.arrive(), request.date());
    }

}
