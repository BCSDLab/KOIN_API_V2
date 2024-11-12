package in.koreatech.koin.domain.bus.global.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.city.service.CityBusService;
import in.koreatech.koin.domain.bus.express.util.ExpressBusService;
import in.koreatech.koin.domain.bus.global.dto.BusTimetableResponse;
import in.koreatech.koin.domain.bus.global.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.global.exception.BusIllegalStationException;
import in.koreatech.koin.domain.bus.global.exception.BusTypeNotFoundException;
import in.koreatech.koin.domain.bus.global.exception.BusTypeNotSupportException;
import in.koreatech.koin.domain.bus.global.model.BusRemainTime;
import in.koreatech.koin.domain.bus.global.model.BusTimetable;
import in.koreatech.koin.domain.bus.global.model.enums.BusType;
import in.koreatech.koin.domain.bus.shuttle.dto.BusCourseResponse;
import in.koreatech.koin.domain.bus.shuttle.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.shuttle.model.BusCourse;
import in.koreatech.koin.domain.bus.shuttle.model.Route;
import in.koreatech.koin.domain.bus.shuttle.model.SchoolBusTimetable;
import in.koreatech.koin.domain.bus.shuttle.model.enums.BusStation;
import in.koreatech.koin.domain.bus.shuttle.repository.SchoolBusRepository;
import in.koreatech.koin.domain.version.dto.VersionResponse;
import in.koreatech.koin.domain.version.service.VersionService;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusService {

    private final Clock clock;
    private final CityBusService cityBusService;
    private final SchoolBusRepository schoolBusRepository;
    private final ExpressBusService expressBusService;
    private final VersionService versionService;

    @Transactional
    public BusRemainTimeResponse getBusRemainTime(BusType busType, BusStation depart, BusStation arrival) {
        validateBusCourse(depart, arrival);
        if (busType == BusType.CITY) {
            return toResponse(busType, cityBusService.getBusRemainTime(depart, arrival));
        }
        if (busType == BusType.EXPRESS) {
            return toResponse(busType, expressBusService.getBusRemainTime(depart, arrival));
        }
        if (busType == BusType.SHUTTLE || busType == BusType.COMMUTING) {
            List<BusCourse> busCourses = schoolBusRepository.findByBusType(busType.getName());
            var remainTimes = busCourses.stream()
                .map(BusCourse::getRoutes)
                .flatMap(routes ->
                    routes.stream()
                        .filter(route -> route.isRunning(clock))
                        .filter(route -> route.isCorrectRoute(depart, arrival, clock))
                        .map(route -> route.getRemainTime(depart))
                )
                .distinct()
                .sorted()
                .toList();
            return toResponse(busType, remainTimes);
        }
        throw new KoinIllegalArgumentException("Invalid bus", "type: " + busType);
    }

    public List<SingleBusTimeResponse> searchTimetable(
        LocalDate date, LocalTime time,
        BusStation depart, BusStation arrival
    ) {
        validateBusCourse(depart, arrival); // 정류장 검증
        List<SingleBusTimeResponse> result = new ArrayList<>();

        LocalDateTime targetTime = LocalDateTime.of(date, time);
        for (BusType busType : BusType.values()) {
            SingleBusTimeResponse busTimeResponse = null;

            if (busType == BusType.EXPRESS) {
                busTimeResponse = expressBusService.searchBusTime(
                    busType.getName(),
                    depart,
                    arrival,
                    targetTime
                );
            }

            if (busType == BusType.SHUTTLE || busType == BusType.COMMUTING) {
                ZonedDateTime zonedAt = targetTime.atZone(clock.getZone());
                Clock clockAt = Clock.fixed(zonedAt.toInstant(), zonedAt.getZone());

                String todayName = targetTime.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.US)
                    .toUpperCase();

                LocalTime arrivalTime = schoolBusRepository.findByBusType(busType.getName()).stream()
                    .filter(busCourse -> busCourse.getRegion().equals("천안"))
                    .map(BusCourse::getRoutes)
                    .flatMap(routes ->
                        routes.stream()
                            .filter(route -> route.getRunningDays().contains(todayName))
                            .filter(route -> route.isRunning(clockAt))
                            .filter(route -> route.isCorrectRoute(depart, arrival, clockAt))
                            .flatMap(route ->
                                route.getArrivalInfos().stream()
                                    .filter(arrivalNode -> depart.getDisplayNames().contains(arrivalNode.getNodeName()))
                            )
                    )
                    .min(Comparator.comparing(o -> LocalTime.parse(o.getArrivalTime())))
                    .map(Route.ArrivalNode::getArrivalTime)
                    .map(LocalTime::parse)
                    .orElse(null);

                busTimeResponse = new SingleBusTimeResponse(busType.getName(), arrivalTime);
            }

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
        if (busType == BusType.CITY) {
            throw BusTypeNotSupportException.withDetail("busType: CITY");
        }

        if (busType == BusType.EXPRESS) {
            return expressBusService.getExpressBusTimetable(direction);
        }

        if (busType == BusType.SHUTTLE || busType == BusType.COMMUTING) {
            BusCourse busCourse = schoolBusRepository
                .getByBusTypeAndDirectionAndRegion(busType.getName(), direction, region);

            return busCourse.getRoutes().stream()
                .map(route -> new SchoolBusTimetable(
                    route.getRouteName(),
                    route.getArrivalInfos().stream()
                        .map(node -> new SchoolBusTimetable.ArrivalNode(
                            node.getNodeName(), node.getArrivalTime())
                        ).toList())).toList();
        }

        throw BusTypeNotFoundException.withDetail(busType.name());
    }

    public BusTimetableResponse getBusTimetableWithUpdatedAt(BusType busType, String direction, String region) {
        List<? extends BusTimetable> busTimetables = getBusTimetable(busType, direction, region);

        if (busType.equals(BusType.COMMUTING)) {
            busType = BusType.SHUTTLE;
        }

        VersionResponse version = versionService.getVersion(busType.getName() + "_bus_timetable");
        return new BusTimetableResponse(busTimetables, version.updatedAt());
    }

    public List<BusCourseResponse> getBusCourses() {
        return schoolBusRepository.findAll().stream()
            .map(BusCourseResponse::from)
            .toList();
    }
}
