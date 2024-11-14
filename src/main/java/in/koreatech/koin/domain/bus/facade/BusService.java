package in.koreatech.koin.domain.bus.facade;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.service.CityBusService;
import in.koreatech.koin.domain.bus.service.ExpressBusService;
import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse;
import in.koreatech.koin.domain.bus.dto.BusTimetableResponse;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.exception.BusIllegalStationException;
import in.koreatech.koin.domain.bus.exception.BusTypeNotSupportException;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.BusTimetable;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.dto.shuttle.BusCourseResponse;
import in.koreatech.koin.domain.bus.dto.shuttle.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.service.ShuttleBusService;
import in.koreatech.koin.domain.version.dto.VersionResponse;
import in.koreatech.koin.domain.version.service.VersionService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusService {

    private final Clock clock;
    private final CityBusService cityBusService;
    private final ExpressBusService expressBusService;
    private final ShuttleBusService shuttleBusService;
    private final BusRouteService busRouteService;
    private final VersionService versionService;

    @Transactional
    public BusRemainTimeResponse getBusRemainTime(BusType busType, BusStation depart, BusStation arrival) {
        validateBusCourse(depart, arrival);
        return switch (busType) {
            case CITY -> toResponse(busType, cityBusService.getBusRemainTime(depart, arrival));
            case EXPRESS -> toResponse(busType, expressBusService.getBusRemainTime(depart, arrival));
            case SHUTTLE, COMMUTING -> toResponse(busType, shuttleBusService.getBusRemainTime(busType, depart, arrival));
        };
    }

    public List<SingleBusTimeResponse> searchTimetable(
        LocalDate date, LocalTime time,
        BusStation depart, BusStation arrival
    ) {
        validateBusCourse(depart, arrival); // 정류장 검증
        List<SingleBusTimeResponse> result = new ArrayList<>();

        LocalDateTime targetTime = LocalDateTime.of(date, time);
        for (BusType busType : BusType.values()) {
            SingleBusTimeResponse busTimeResponse = switch (busType) {
                case EXPRESS -> expressBusService.searchBusTime(busType.getName(), depart, arrival, targetTime);
                case SHUTTLE, COMMUTING -> shuttleBusService.searchShuttleBusTime(date, time, depart, arrival, busType);
                default -> null;
            };

            if (busTimeResponse == null) {
                continue;
            }
            result.add(busTimeResponse);
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

    private void validateBusCourse(BusStation depart, BusStation arrival) {
        if (depart.equals(arrival)) {
            throw BusIllegalStationException.withDetail("depart: " + depart.name() + ", arrival: " + arrival.name());
        }
    }

    public List<? extends BusTimetable> getBusTimetable(BusType busType, String direction, String region) {
        return switch (busType) {
            case CITY -> throw BusTypeNotSupportException.withDetail("busType: CITY");
            case EXPRESS -> expressBusService.getExpressBusTimetable(direction);
            case SHUTTLE, COMMUTING -> shuttleBusService.getShuttleBusTimetable(busType, direction, region);
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

    public BusScheduleResponse getBusSchedule(BusRouteCommand request) {
        List<BusScheduleResponse.ScheduleInfo> scheduleInfoList = switch(request.depart()) {
            case KOREATECH -> busRouteService.getBusScheduleDepartFromKoreaTech(request);
            case TERMINAL, STATION -> busRouteService.getBusScheduleDepartFromElse(request, request.depart());
        };
        return new BusScheduleResponse(
            request.depart(), request.arrive(), request.date(), request.time(), scheduleInfoList
        );
    }

    public List<BusCourseResponse> getBusCourses() {
        return shuttleBusService.getShuttleBusCourses();
    }
}
