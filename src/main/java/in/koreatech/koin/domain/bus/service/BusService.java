package in.koreatech.koin.domain.bus.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.dto.BusCourseResponse;
import in.koreatech.koin.domain.bus.dto.BusNoticeResponse;
import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.dto.BusTimetableResponse;
import in.koreatech.koin.domain.bus.service.city.dto.CityBusTimetableResponse;
import in.koreatech.koin.domain.bus.service.shuttle.dto.ShuttleBusRoutesResponse;
import in.koreatech.koin.domain.bus.service.shuttle.dto.ShuttleBusTimetableResponse;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.BusType;
import in.koreatech.koin.domain.bus.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.exception.BusIllegalStationException;
import in.koreatech.koin.domain.bus.exception.BusTypeNotSupportException;
import in.koreatech.koin.domain.bus.service.city.CityBusService;
import in.koreatech.koin.domain.bus.service.express.ExpressBusService;
import in.koreatech.koin.domain.bus.service.model.BusRemainTime;
import in.koreatech.koin.domain.bus.service.model.BusTimetable;
import in.koreatech.koin.domain.bus.service.model.route.BusRouteStrategy;
import in.koreatech.koin.domain.bus.service.shuttle.ShuttleBusService;
import in.koreatech.koin.domain.version.dto.VersionResponse;
import in.koreatech.koin.domain.version.service.VersionService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusService {

    private final Clock clock;
    private final BusNoticeRepository busNoticeRepository;
    private final VersionService versionService;
    private final List<BusRouteStrategy> busRouteStrategies;
    private final ExpressBusService expressBusService;
    private final CityBusService cityBusService;
    private final ShuttleBusService shuttleBusService;

    @Transactional
    public BusRemainTimeResponse getBusRemainTime(BusType busType, BusStation depart, BusStation arrival) {
        checkDuplicateStations(depart, arrival);

        var remainTimes = switch (busType) {
            case CITY -> cityBusService.getBusRemainTime(depart, arrival);
            case EXPRESS -> expressBusService.getBusRemainTime(depart, arrival);
            case SHUTTLE, COMMUTING -> shuttleBusService.getShuttleBusRemainTimes(busType, depart, arrival);
        };

        return toResponse(busType, remainTimes);
    }

    public List<SingleBusTimeResponse> searchTimetable(
        LocalDate date, LocalTime time,
        BusStation depart, BusStation arrival
    ) {
        checkDuplicateStations(depart, arrival);
        List<SingleBusTimeResponse> result = new ArrayList<>();

        LocalDateTime targetTime = LocalDateTime.of(date, time);

        for (BusType busType : BusType.values()) {
            SingleBusTimeResponse busTimeResponse = switch (busType) {
                case EXPRESS -> expressBusService.searchBusTime(busType.getName(), depart, arrival, targetTime);
                case SHUTTLE, COMMUTING -> shuttleBusService.searchShuttleBusTime(depart, arrival, busType, targetTime);
                default -> null;
            };

            if (busTimeResponse != null) {
                result.add(busTimeResponse);
            }
        }

        return result;
    }

    private BusRemainTimeResponse toResponse(BusType busType, List<? extends BusRemainTime> remainTimes) {
        return BusRemainTimeResponse.of(
            busType,
            remainTimes.stream()
                .filter(bus -> bus.getRemainSeconds(clock) != null)
                .sorted(Comparator.naturalOrder())
                .toList(),
            clock
        );
    }

    private void checkDuplicateStations(BusStation depart, BusStation arrival) {
        if (depart.equals(arrival)) {
            throw BusIllegalStationException.withDetail("depart: " + depart.name() + ", arrival: " + arrival.name());
        }
    }

    public List<? extends BusTimetable> getBusTimetable(BusType busType, String direction, String region) {
        return switch (busType) {
            case CITY -> throw BusTypeNotSupportException.withDetail("busType: CITY");
            case EXPRESS -> expressBusService.getExpressBusTimetable(direction);
            case SHUTTLE, COMMUTING -> shuttleBusService.getSchoolBusTimetables(busType, direction, region);
        };
    }

    public BusTimetableResponse getBusTimetableWithUpdatedAt(BusType busType, String direction, String region) {
        List<? extends BusTimetable> busTimetables = getBusTimetable(busType, direction, region);

        if (busType.equals(BusType.COMMUTING)) {
            busType = BusType.SHUTTLE;
        }

        VersionResponse version = versionService.getVersion(busType.getName() + "_bus_timetable");
        return new BusTimetableResponse(busTimetables, version.updatedAt());
    }

    public CityBusTimetableResponse getCityBusTimetable(Long busNumber, CityBusDirection direction) {
        return cityBusService.getCityBusTimetable(busNumber, direction);
    }

    public BusScheduleResponse getBusSchedule(BusRouteCommand request) {
        List<ScheduleInfo> scheduleInfoList = Collections.emptyList();

        if (request.checkAvailableCourse()) {
            scheduleInfoList = busRouteStrategies.stream()
                .filter(strategy -> strategy.support(request.busRouteType()))
                .flatMap(strategy -> strategy.findSchedule(request).stream())
                .filter(schedule -> schedule.departTime().isAfter(request.time()))
                .sorted(Comparator.comparing(ScheduleInfo::departTime)
                    .thenComparing(ScheduleInfo.compareBusType())
                )
                .toList();
        }

        return new BusScheduleResponse(
            request.depart(), request.arrive(), request.date(), request.time(),
            scheduleInfoList
        );
    }

    public BusNoticeResponse getNotice() {
        Map<Object, Object> article = busNoticeRepository.getBusNotice();

        if (article == null || article.isEmpty()) {
            return null;
        }

        return BusNoticeResponse.of(
            (Integer)article.get("id"),
            (String)article.get("title")
        );
    }

    public List<BusCourseResponse> getBusCourses() {
        return shuttleBusService.getBusCourses();
    }

    public ShuttleBusRoutesResponse getShuttleBusRoutes() {
        return shuttleBusService.getShuttleBusRoutes();
    }

    public ShuttleBusTimetableResponse getShuttleBusTimetable(String id) {
        return shuttleBusService.getShuttleBusTimetable(id);
    }
}
