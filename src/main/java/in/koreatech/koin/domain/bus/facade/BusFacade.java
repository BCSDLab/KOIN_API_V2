package in.koreatech.koin.domain.bus.facade;

import static in.koreatech.koin.domain.bus.model.enums.BusType.COMMUTING;
import static in.koreatech.koin.domain.bus.model.enums.BusType.SHUTTLE;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.dto.BusTimetable;
import in.koreatech.koin.domain.bus.dto.BusTimetableResponse;
import in.koreatech.koin.domain.bus.dto.SingleArrivalTimeResponse;
import in.koreatech.koin.domain.bus.dto.city.CityBusTimetableResponse;
import in.koreatech.koin.domain.bus.dto.shuttle.BusCourseResponse;
import in.koreatech.koin.domain.bus.exception.BusIllegalStationException;
import in.koreatech.koin.domain.bus.exception.BusTypeNotSupportException;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.service.CityBusService;
import in.koreatech.koin.domain.bus.service.ExpressBusService;
import in.koreatech.koin.domain.bus.service.ShuttleBusService;
import in.koreatech.koin.domain.bus.service.route.BusRouteStrategy;
import in.koreatech.koin.domain.version.dto.VersionResponse;
import in.koreatech.koin.domain.version.service.VersionService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusFacade {

    private final List<BusRouteStrategy> busRouteStrategies;
    private final Clock clock;
    private final CityBusService cityBusService;
    private final ExpressBusService expressBusService;
    private final ShuttleBusService shuttleBusService;
    private final VersionService versionService;

    public BusRemainTimeResponse getBusRemainTime(BusType busType, BusStation depart, BusStation arrival) {
        validateBusCourse(depart, arrival);
        List<? extends BusRemainTime> busRemainTimes = switch (busType) {
            case CITY -> cityBusService.getBusRemainTime(depart, arrival);
            case EXPRESS -> expressBusService.getBusRemainTime(depart, arrival);
            case SHUTTLE, COMMUTING -> shuttleBusService.getBusRemainTime(busType, depart, arrival);
        };
        return BusRemainTimeResponse.of(busType, getResolvedRemainTimes(busRemainTimes), clock);
    }

    private void validateBusCourse(BusStation depart, BusStation arrival) {
        if (depart.equals(arrival)) {
            throw BusIllegalStationException.withDetail("depart: " + depart.name() + ", arrival: " + arrival.name());
        }
    }

    private List<? extends BusRemainTime> getResolvedRemainTimes(List<? extends BusRemainTime> remainTimes) {
        return remainTimes.stream()
            .filter(bus -> bus.getRemainSeconds(clock) != null)
            .sorted(Comparator.naturalOrder())
            .toList();
    }

    public List<SingleArrivalTimeResponse> searchNearestBusArrivals(
        LocalDate date, LocalTime time, BusStation depart, BusStation arrival
    ) {
        validateBusCourse(depart, arrival);
        LocalDateTime targetTime = LocalDateTime.of(date, time);
        List<SingleArrivalTimeResponse> arrivalTImeResponseList = new ArrayList<>();
        arrivalTImeResponseList.add(expressBusService.searchBusTime(depart, arrival, targetTime));
        arrivalTImeResponseList.add(shuttleBusService.searchShuttleBusTime(date, time, depart, arrival, SHUTTLE));
        arrivalTImeResponseList.add(shuttleBusService.searchShuttleBusTime(date, time, depart, arrival, COMMUTING));
        // CityBus 제외
        return arrivalTImeResponseList;
    }

    public List<? extends BusTimetable> getBusTimetable(BusType busType, String direction, String region) {
        return switch (busType) {
            case CITY -> throw BusTypeNotSupportException.withDetail("busType: CITY");
            case EXPRESS -> expressBusService.getExpressBusTimetable(direction);
            case SHUTTLE, COMMUTING -> shuttleBusService.getShuttleBusTimetable(busType, direction, region);
        };
    }

    public CityBusTimetableResponse getCityBusTimetable(Long busNumber, CityBusDirection direction) {
        return cityBusService.getCityBusTimetable(busNumber, direction);
    }

    public BusTimetableResponse getBusTimetableWithUpdatedAt(BusType busType, String direction, String region) {
        List<? extends BusTimetable> busTimetables = getBusTimetable(busType, direction, region);
        BusType resolvedBusType = busType.equals(COMMUTING) ? SHUTTLE : busType;
        String versionType = resolvedBusType.getName() + "_bus_timetable";
        VersionResponse version = versionService.getVersion(versionType);
        return new BusTimetableResponse(busTimetables, version.updatedAt());
    }

    public List<BusCourseResponse> getShuttleBusCourses() {
        return shuttleBusService.getShuttleBusCourses();
    }

    public BusScheduleResponse getBusSchedule(BusRouteCommand request) {
        List<ScheduleInfo> scheduleInfoList = busRouteStrategies.stream()
            .filter(strategy -> strategy.support(request.busRouteType()))
            .flatMap(strategy -> strategy.findSchedule(request).stream())
            .filter(schedule -> schedule.departTime().isAfter(request.time()))
            .sorted(Comparator.comparing(ScheduleInfo::departTime))
            .toList();

        return new BusScheduleResponse(
            request.depart(), request.arrive(), request.date(), request.time(),
            scheduleInfoList
        );
    }
}
