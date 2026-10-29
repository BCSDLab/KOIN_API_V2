package in.koreatech.koin.domain.bus.service.shuttle;

import static in.koreatech.koin.domain.bus.enums.ShuttleBusRegion.CHEONAN_ASAN;
import static in.koreatech.koin.domain.version.model.VersionType.SHUTTLE;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
import in.koreatech.koin.domain.bus.service.shuttle.internal.ShuttleRoutePathSelector;
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

    private static final DateTimeFormatter SCHEDULE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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
        List<Route> routes = getShuttleRoutesByBusType(busType, true, false);
        LocalTime currentTime = LocalTime.now(clock);

        return routes.stream()
            .filter(route -> route.isRunning(clock))
            .flatMap(route -> route.findOccurrences(depart, arrival).stream())
            .filter(occurrence -> occurrence.departureTime().isAfter(currentTime))
            .map(occurrence -> BusRemainTime.from(occurrence.departureTime().format(SCHEDULE_TIME_FORMATTER)))
            .filter(remainTime -> remainTime.getBusArrivalTime() != null)
            .distinct()
            .sorted()
            .toList();
    }

    public List<SchoolBusTimetable> getSchoolBusTimetables(BusType busType, String direction, String region) {
        ShuttleBusRegion busRegion = ShuttleBusRegion.convertFrom(region);
        String busDirection = BusDirection.from(direction).getName();

        return getShuttleRoutesByBusType(busType, false, true).stream()
            .filter(route -> route.getDirection().equals(busDirection))
            .filter(route -> route.getRegion().equals(busRegion))
            .map(SchoolBusTimetable::new)
            .toList();
    }

    public SingleBusTimeResponse searchShuttleBusTime(BusStation depart, BusStation arrival, BusType busType,
        LocalDateTime targetTime) {
        ZonedDateTime zonedAt = targetTime.atZone(clock.getZone());
        Clock clockAt = Clock.fixed(zonedAt.toInstant(), zonedAt.getZone());

        List<Route> routes = getShuttleRoutesByBusType(busType, true, false).stream()
            .filter(route -> route.isRunning(clockAt))
            .toList();

        LocalTime arrivalTime = routes.stream()
            .flatMap(route -> route.findOccurrences(depart, arrival).stream())
            .map(ShuttleRoutePathSelector.Occurrence::departureTime)
            .filter(departureTime -> departureTime.isAfter(targetTime.toLocalTime()))
            .min(Comparator.naturalOrder())
            .orElse(null);

        return new SingleBusTimeResponse(busType.getName(), arrivalTime);
    }

    private List<Route> getShuttleRoutesByBusType(BusType busType, boolean cheonanOnly, boolean normalizeForDisplay) {
        String semester = versionService.getVersionEntity(SHUTTLE).getTitle();
        List<ShuttleRouteType> routeTypes = ShuttleRouteType.convertFrom(busType);
        List<Route> routes = routeTypes.stream()
            .flatMap(routeType ->
                shuttleBusRepository.findAllBySemesterTypeAndRouteType(semester, routeType).stream())
            .toList();
        if (cheonanOnly) {
            routes = routes.stream()
                .filter(route -> CHEONAN_ASAN.equals(route.getRegion()))
                .toList();
        }
        if (normalizeForDisplay) {
            routes.forEach(Route::sortArrivalNodesByDirection);
        }
        return routes;
    }
}
