package in.koreatech.koin.domain.bus.service.shuttle;

import static in.koreatech.koin.domain.bus.enums.ShuttleBusRegion.CHEONAN_ASAN;
import static in.koreatech.koin.domain.version.model.VersionType.SHUTTLE;

import java.time.Clock;
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

import in.koreatech.koin.domain.bus.dto.BusCourseResponse;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.enums.BusDirection;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.BusType;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteName;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.service.model.BusRemainTime;
import in.koreatech.koin.domain.bus.service.shuttle.dto.ShuttleBusRoutesResponse;
import in.koreatech.koin.domain.bus.service.shuttle.dto.ShuttleBusTimetableResponse;
import in.koreatech.koin.domain.bus.service.shuttle.model.ArrivalNode;
import in.koreatech.koin.domain.bus.service.shuttle.model.Route;
import in.koreatech.koin.domain.bus.service.shuttle.model.SchoolBusTimetable;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.domain.version.dto.VersionMessageResponse;
import in.koreatech.koin.domain.version.service.VersionService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShuttleBusService {

    private final VersionService versionService;
    private final ShuttleBusRepository shuttleBusRepository;
    private final Clock clock;

    public List<BusCourseResponse> getBusCourses() {
        List<BusCourseResponse> courses = new ArrayList<>();
        for (var region : ShuttleRouteName.values()) {
            for (var direction : BusDirection.values()) {
                courses.add(BusCourseResponse.of(region, direction));
            }
        }
        return courses;
    }

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
        List<Route> routes = getShuttleRoutesByBusType(busType);

        return routes.stream()
            .filter(route -> route.isRunning(clock))
            .filter(route -> route.isCorrectRoute(depart, arrival, clock))
            .map(route -> route.getRemainTime(depart))
            .distinct()
            .sorted()
            .toList();
    }

    public List<SchoolBusTimetable> getSchoolBusTimetables(BusType busType, String direction, String region) {
        ShuttleBusRegion busRegion = ShuttleBusRegion.convertFrom(region);
        String busDirection = BusDirection.from(direction).getName();

        return getShuttleRoutesByBusType(busType).stream()
            .filter(route -> route.getDirection().equals(busDirection))
            .filter(route -> route.getRegion().equals(busRegion))
            .map(SchoolBusTimetable::new)
            .toList();
    }

    public SingleBusTimeResponse searchShuttleBusTime(BusStation depart, BusStation arrival, BusType busType,
        LocalDateTime targetTime) {
        ZonedDateTime zonedAt = targetTime.atZone(clock.getZone());
        Clock clockAt = Clock.fixed(zonedAt.toInstant(), zonedAt.getZone());

        String todayName = targetTime.getDayOfWeek()
            .getDisplayName(TextStyle.SHORT, Locale.US)
            .toUpperCase();

        List<Route> routes = getShuttleRoutesByBusType(busType).stream()
            .filter(route -> route.getRegion().equals(CHEONAN_ASAN))
            .filter(route -> route.getRunningDays().contains(todayName))
            .filter(route -> route.isRunning(clockAt))
            .filter(route -> route.isCorrectRoute(depart, arrival, clockAt))
            .toList();

        LocalTime arrivalTime = routes.stream()
            .flatMap(route ->
                route.getArrivalNodes().stream()
                    .filter(arrivalNode -> depart.getDisplayNames().contains(arrivalNode.getNodeName()))
            )
            .min(Comparator.comparing(o -> LocalTime.parse(o.getArrivalTime())))
            .map(ArrivalNode::getArrivalTime)
            .map(LocalTime::parse)
            .orElse(null);

        return new SingleBusTimeResponse(busType.getName(), arrivalTime);
    }

    private List<Route> getShuttleRoutesByBusType(BusType busType) {
        String semester = versionService.getVersionEntity(SHUTTLE).getTitle();
        List<ShuttleRouteType> routeTypes = ShuttleRouteType.convertFrom(busType);
        List<Route> routes = routeTypes.stream()
            .flatMap(routeType ->
                shuttleBusRepository.findAllBySemesterTypeAndRouteType(semester, routeType).stream())
            .toList();
        routes.forEach(Route::sortArrivalNodesByDirection);
        return routes;
    }
}
