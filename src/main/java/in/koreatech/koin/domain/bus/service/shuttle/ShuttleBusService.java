package in.koreatech.koin.domain.bus.service.shuttle;

import static in.koreatech.koin.domain.bus.enums.ShuttleBusRegion.CHEONAN_ASAN;
import static in.koreatech.koin.domain.bus.enums.ShuttleRouteType.WEEKDAYS;
import static in.koreatech.koin.domain.bus.enums.ShuttleRouteType.WEEKEND;
import static in.koreatech.koin.domain.bus.enums.ShuttleRouteType.SHUTTLE;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.dto.BusCourseResponse;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.enums.BusDirection;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.BusType;
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
        for (var routeName : ShuttleRouteName.values()) {
            for (var direction : BusDirection.values()) {
                courses.add(BusCourseResponse.of(routeName, direction));
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
        List<ShuttleBusRoute> shuttleBusRoutes = getShuttleBusRoutes(busType);
        List<List<Route>> routeList = shuttleBusRoutes.stream()
            .map(this::routeConvertor)
            .toList();

        return routeList.stream()
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
        ShuttleRouteName routeName = ShuttleRouteName.getRegionByLegacy(busType, region);
        String semester = versionService.getVersionEntity("shuttle_bus_timetable").getTitle();
        String busDirection = BusDirection.from(direction).getName();

        List<Route> routes = routeName.getNewBusType().stream()
            .flatMap(routeType -> shuttleBusRepository.findAllByRegionAndRouteTypeAndSemesterType(
                routeName.getNewRegionName(), routeType, semester).stream())
            .flatMap(route -> routeConvertor(route).stream())
            .filter(route -> route.getDirection().equals(busDirection))
            .toList();

        return routes.stream()
            .map(route -> new SchoolBusTimetable(route.getRouteName(), route.getArrivalInfos()))
            .toList();
    }

    public SingleBusTimeResponse searchShuttleBusTime(BusStation depart, BusStation arrival, BusType busType,
        LocalDateTime targetTime) {
        ZonedDateTime zonedAt = targetTime.atZone(clock.getZone());
        Clock clockAt = Clock.fixed(zonedAt.toInstant(), zonedAt.getZone());

        String todayName = targetTime.getDayOfWeek()
            .getDisplayName(TextStyle.SHORT, Locale.US)
            .toUpperCase();

        List<ShuttleBusRoute> routeList = getShuttleBusRoutes(busType);
        LocalTime arrivalTime = routeList.stream()
            .filter(busRoute -> busRoute.getRegion().equals(CHEONAN_ASAN))
            .map(this::routeConvertor)
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
            .map(ArrivalNode::getArrivalTime)
            .map(LocalTime::parse)
            .orElse(null);

        return new SingleBusTimeResponse(busType.getName(), arrivalTime);
    }

    private List<ShuttleBusRoute> getShuttleBusRoutes(BusType busType) {
        List<ShuttleBusRoute> routes = new ArrayList<>();
        if (busType.equals(BusType.COMMUTING)) {
            routes.addAll(shuttleBusRepository.findAllByRouteType(WEEKDAYS));
        }
        if (busType.equals(BusType.SHUTTLE)) {
            for (var shuttleBusType : List.of(SHUTTLE, WEEKEND)) {
                routes.addAll(shuttleBusRepository.findAllByRouteType(shuttleBusType));
            }
        }
        return routes;
    }

    private List<Route> routeConvertor(ShuttleBusRoute shuttleBusRoute) {
        return shuttleBusRoute.getRouteInfo().stream()
            .map(routeInfo ->
                Route.builder()
                    .routeName(shuttleBusRoute.getRouteName())
                    .routeType(shuttleBusRoute.getRouteType())
                    .direction(extractDirection(shuttleBusRoute, routeInfo))
                    .runningDays(routeInfo.getRunningDays())
                    .arrivalInfos(
                        IntStream.range(0, routeInfo.getArrivalTime().size()).mapToObj(i ->
                                ArrivalNode.builder()
                                    .nodeName(shuttleBusRoute.getNodeInfo().get(i).getName())
                                    .arrivalTime(routeInfo.getArrivalTime().get(i))
                                    .build())
                            .toList())
                    .build())
            .toList();
    }

    private String extractDirection(ShuttleBusRoute busRoute, ShuttleBusRoute.RouteInfo routeInfo) {
        ShuttleRouteType busType = busRoute.getRouteType();
        if (busType.equals(WEEKDAYS)) {
            return routeInfo.getName();
        }
        if (busType.equals(WEEKEND)) {
            return routeInfo.getDetail();
        }
        return routeInfo.getArrivalTime().get(0) == null ? "등교" : "하교";
    }
}
