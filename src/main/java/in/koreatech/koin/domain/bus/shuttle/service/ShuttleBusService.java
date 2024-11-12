package in.koreatech.koin.domain.bus.shuttle.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.global.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.global.model.BusRemainTime;
import in.koreatech.koin.domain.bus.global.model.enums.BusType;
import in.koreatech.koin.domain.bus.shuttle.dto.BusCourseResponse;
import in.koreatech.koin.domain.bus.shuttle.model.BusCourse;
import in.koreatech.koin.domain.bus.shuttle.model.Route;
import in.koreatech.koin.domain.bus.shuttle.model.SchoolBusTimetable;
import in.koreatech.koin.domain.bus.shuttle.model.enums.BusStation;
import in.koreatech.koin.domain.bus.shuttle.repository.SchoolBusRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShuttleBusService {

    private final Clock clock;
    private final SchoolBusRepository schoolBusRepository;

    @Transactional
    public List<BusRemainTime> getBusRemainTime(BusType busType, BusStation depart, BusStation arrival) {
        List<BusCourse> busCourses = schoolBusRepository.findByBusType(busType.getName());
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

        return new SingleBusTimeResponse(busType.getName(), arrivalTime);
    }

    public List<SchoolBusTimetable> getShuttleBusTimetable(BusType busType, String direction, String region) {
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

    public List<BusCourseResponse> getShuttleBusCourses() {
        return schoolBusRepository.findAll().stream()
            .map(BusCourseResponse::from)
            .toList();
    }


}
