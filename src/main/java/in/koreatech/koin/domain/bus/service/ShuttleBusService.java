package in.koreatech.koin.domain.bus.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.dto.shuttle.BusCourseResponse;
import in.koreatech.koin.domain.bus.service.route.ShuttleBusRouteManager;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.model.shuttle.BusCourse;
import in.koreatech.koin.domain.bus.model.shuttle.Route;
import in.koreatech.koin.domain.bus.dto.shuttle.SchoolBusTimetable;
import in.koreatech.koin.domain.bus.repository.ShuttleBusRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShuttleBusService {

    private final Clock clock;
    private final ShuttleBusRepository shuttleBusRepository;

    @Transactional
    public List<BusRemainTime> getBusRemainTime(BusType busType, BusStation depart, BusStation arrival) {
        List<BusCourse> busCourses = shuttleBusRepository.findByBusType(busType.getName());
        return busCourses.stream()
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
    }

    public SingleBusTimeResponse searchShuttleBusTime(
        LocalDate date, LocalTime time,
        BusStation depart, BusStation arrival, BusType busType
    ) {
        LocalDateTime targetTime = LocalDateTime.of(date, time);

        ZonedDateTime zonedAt = targetTime.atZone(clock.getZone());
        Clock clockAt = Clock.fixed(zonedAt.toInstant(), zonedAt.getZone());

        String todayName = targetTime.getDayOfWeek()
            .getDisplayName(TextStyle.SHORT, Locale.US)
            .toUpperCase();

        LocalTime arrivalTime = shuttleBusRepository.findByBusType(busType.getName()).stream()
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

        return new SingleBusTimeResponse(busType.getName(), arrivalTime);
    }

    public List<SchoolBusTimetable> getShuttleBusTimetable(BusType busType, String direction, String region) {
        BusCourse busCourse = shuttleBusRepository
            .getByBusTypeAndDirectionAndRegion(busType.getName(), direction, region);

        return busCourse.getRoutes().stream()
            .map(route -> new SchoolBusTimetable(
                route.getRouteName(),
                route.getArrivalInfos().stream()
                    .map(node -> new SchoolBusTimetable.ArrivalNode(
                        node.getNodeName(), node.getArrivalTime())
                    ).toList())).toList();
    }

    public List<BusCourseResponse> getShuttleBusCourses() {
        return shuttleBusRepository.findAll().stream()
            .map(BusCourseResponse::from)
            .toList();
    }

    public List<BusScheduleResponse.ScheduleInfo> getShuttleBusSchedule(BusRouteCommand request, BusType busType) {
        BusCourse busFrom = shuttleBusRepository.getByBusTypeAndDirectionAndRegion(
            busType.getName(), "from", "천안"
        );
        BusCourse busTo = shuttleBusRepository.getByBusTypeAndDirectionAndRegion(
            busType.getName(), "to", "천안"
        );

        return switch (busType) {
            case SHUTTLE -> ShuttleBusRouteManager.getShuttleBusSchedule(
                busFrom, busTo, request.depart(), request.arrive(), request.date()
            );
            case COMMUTING -> ShuttleBusRouteManager.getCommutingBusSchedule(
                busFrom, busTo, request.depart(), request.arrive(), request.date()
            );
            default -> Collections.emptyList();
        };
    }
}
