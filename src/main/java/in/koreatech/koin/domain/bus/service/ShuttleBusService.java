package in.koreatech.koin.domain.bus.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.dto.ShuttleBusRoutesResponse;
import in.koreatech.koin.domain.bus.dto.ShuttleBusTimetableResponse;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.SchoolBusTimetable;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.model.mongo.BusCourse;
import in.koreatech.koin.domain.bus.model.mongo.Route;
import in.koreatech.koin.domain.bus.model.mongo.ShuttleBusRoute;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import in.koreatech.koin.domain.bus.repository.ShuttleBusRepository;
import in.koreatech.koin.domain.version.dto.VersionMessageResponse;
import in.koreatech.koin.domain.version.service.VersionService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShuttleBusService {

    private final ShuttleBusRepository shuttleBusRepository;
    private final VersionService versionService;
    private final BusRepository busRepository;
    private final Clock clock;

    public ShuttleBusRoutesResponse getShuttleBusRoutes() {
        VersionMessageResponse version = versionService.getVersionWithMessage("shuttle_bus_timetable");
        List<ShuttleBusRoute> shuttleBusRoutes = shuttleBusRepository.findBySemesterType(version.title());
        return ShuttleBusRoutesResponse.of(shuttleBusRoutes, version);
    }

    public ShuttleBusTimetableResponse getShuttleBusTimetable(String id) {
        ShuttleBusRoute shuttleBusRoute = shuttleBusRepository.getById(id);
        return ShuttleBusTimetableResponse.from(shuttleBusRoute);
    }

    public List<BusRemainTime> getShuttleBusRemainTimes(BusType busType, BusStation depart, BusStation arrival) {
        List<BusCourse> busCourses = busRepository.findByBusType(busType.getName());

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

    public List<SchoolBusTimetable> getSchoolBusTimetables(BusType busType, String direction, String region) {
        BusCourse busCourse = busRepository
            .getByBusTypeAndDirectionAndRegion(busType.getName(), direction, region);

        return busCourse.getRoutes().stream()
            .map(route -> new SchoolBusTimetable(
                route.getRouteName(),
                route.getArrivalInfos().stream()
                    .map(node -> new SchoolBusTimetable.ArrivalNode(
                        node.getNodeName(), node.getArrivalTime())
                    ).toList())).toList();
    }

    public SingleBusTimeResponse searchShuttleBusTime(BusStation depart, BusStation arrival, BusType busType,
        LocalDateTime targetTime) {
        SingleBusTimeResponse busTimeResponse;
        ZonedDateTime zonedAt = targetTime.atZone(clock.getZone());
        Clock clockAt = Clock.fixed(zonedAt.toInstant(), zonedAt.getZone());

        String todayName = targetTime.getDayOfWeek()
            .getDisplayName(TextStyle.SHORT, Locale.US)
            .toUpperCase();

        LocalTime arrivalTime = busRepository.findByBusType(busType.getName()).stream()
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
        return busTimeResponse;
    }
}
