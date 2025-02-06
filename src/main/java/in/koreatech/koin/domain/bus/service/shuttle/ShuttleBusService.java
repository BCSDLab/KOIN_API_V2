package in.koreatech.koin.domain.bus.service.shuttle;

import static in.koreatech.koin.domain.bus.enums.BusType.COMMUTING;
import static in.koreatech.koin.domain.bus.enums.BusType.SHUTTLE;
import static in.koreatech.koin.domain.bus.enums.ShuttleBusRegion.CHEONAN_ASAN;
import static in.koreatech.koin.domain.bus.enums.ShuttleRouteType.WEEKDAYS;
import static in.koreatech.koin.domain.bus.enums.ShuttleRouteType.WEEKEND;

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
import in.koreatech.koin.domain.bus.dto.ShuttleBusRoutesResponse;
import in.koreatech.koin.domain.bus.dto.ShuttleBusTimetableResponse;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.enums.BusDirection;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.BusType;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteName;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.service.model.BusRemainTime;
import in.koreatech.koin.domain.bus.service.shuttle.model.BusCourse;
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
                courses.add(
                    new BusCourseResponse(routeName.getBusType().toString().toLowerCase(), direction.getDirect(),
                        routeName.getRegionName()));
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
        List<BusCourse> busCourses = new ArrayList<>();
        List<ShuttleBusRoute> shuttleBusRoutes = new ArrayList<>();
        if (busType.equals(COMMUTING)) {
            shuttleBusRoutes.addAll(shuttleBusRepository.findAllByRouteType(WEEKDAYS));
        } else {
            for (var type : List.of(WEEKEND, ShuttleRouteType.SHUTTLE)) {
                shuttleBusRoutes.addAll(shuttleBusRepository.findAllByRouteType(type));
            }
        }

        List<List<Route>> routess = new ArrayList<>();
        for (ShuttleBusRoute shuttleBusRoute : shuttleBusRoutes) {
            routess.add(busRouteConvertor(shuttleBusRoute));
        }

        return routess.stream()
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

    private List<Route> busRouteConvertor(ShuttleBusRoute shuttleBusRoute) {
        List<ShuttleBusRoute.RouteInfo> routeInfos = shuttleBusRoute.getRouteInfo();
        List<Route> routes = new ArrayList<>();
        for (var routeInfo : routeInfos) {
            List<Route.ArrivalNode> arrivalNodes = new ArrayList<>();
            List<String> arrivalTimes = routeInfo.getArrivalTime();

            //TODO: 등교/하교별 다른 정렬 처리
            for (int i = 0; i < arrivalTimes.size(); i++) {
                arrivalNodes.add(Route.ArrivalNode.builder()
                    .nodeName(shuttleBusRoute.getNodeInfo().get(i).getName())
                    .arrivalTime(arrivalTimes.get(i))
                    .build());
            }
            Route route = Route.builder()
                .routeName(shuttleBusRoute.getRouteName())
                .runningDays(routeInfo.getRunningDays())
                .arrivalInfos(arrivalNodes)
                .build();
            routes.add(route);
        }

        return routes;
    }

    public List<SchoolBusTimetable> getSchoolBusTimetables(BusType busType, String direction, String region) {
        List<SchoolBusTimetable> timetableList = new ArrayList<>();

        // 기존 요청으로 최신 db에 맞는 요청으로 변환
        ShuttleRouteName routeName = ShuttleRouteName.getRegionByLegacy(busType, region);
        String semester = versionService.getVersionEntity("shuttle_bus_timetable").getTitle();

        // ex) 통학-천안 다 가져오기
        List<ShuttleBusRoute> shuttleBusRoutes = routeName.getRouteTypes().stream()
            .map(routeType -> shuttleBusRepository.findAllByRegionAndRouteTypeAndSemesterType(
                routeName.getBusRegion(), routeType, semester))
            .flatMap(List::stream)
            .toList();

        // to -> 하교 / from -> 등교 변환
        BusDirection busDirection = BusDirection.from(direction);

        // 등교/하교 필터링 필요
        for (var shuttleBusRoute : shuttleBusRoutes) {
            List<String> nodeInfo = shuttleBusRoute.getNodeInfo().stream()
                .map(ShuttleBusRoute.NodeInfo::getName)
                .toList();
            List<ShuttleBusRoute.RouteInfo> routeInfos = new ArrayList<>();

            if (busType.equals(COMMUTING)) {
                routeInfos.addAll(shuttleBusRoute.getRouteInfo().stream()
                    .filter(direct -> direct.getName().equals(busDirection.getName()))
                    .toList());
            }
            if (busType.equals(SHUTTLE)) {
                if (shuttleBusRoute.getRouteName().contains("일학습병행대학")) {
                    routeInfos.addAll(shuttleBusRoute.getRouteInfo().stream()
                        .filter(direct -> direct.getName().equals(busDirection.getName()))
                        .toList());
                } else {
                    routeInfos.addAll(shuttleBusRoute.getRouteInfo().stream()
                        .filter(direct -> (direct.getArrivalTime().get(0) == null) == busDirection.isNull())
                        .toList());
                }
            }

            for (var routeInfo : routeInfos) {
                List<SchoolBusTimetable.ArrivalNode> arrivalNodes = new ArrayList<>();
                for (int i = 0; i < routeInfo.getArrivalTime().size(); i++) {
                    arrivalNodes.add(new SchoolBusTimetable.ArrivalNode(nodeInfo.get(i),
                        routeInfo.getArrivalTime().get(i)));
                }
                timetableList.add(new SchoolBusTimetable(shuttleBusRoute.getRouteName(), arrivalNodes));
            }
        }

        return timetableList;
    }

    public SingleBusTimeResponse searchShuttleBusTime(BusStation depart, BusStation arrival, BusType busType,
        LocalDateTime targetTime) {
        SingleBusTimeResponse busTimeResponse;
        ZonedDateTime zonedAt = targetTime.atZone(clock.getZone());
        Clock clockAt = Clock.fixed(zonedAt.toInstant(), zonedAt.getZone());

        String todayName = targetTime.getDayOfWeek()
            .getDisplayName(TextStyle.SHORT, Locale.US)
            .toUpperCase();

        List<ShuttleBusRoute> busRoutes = new ArrayList<>();
        if (busType.equals(COMMUTING)) {
            busRoutes.addAll(shuttleBusRepository.findAllByRouteType(WEEKDAYS));
        }
        if (busType.equals(SHUTTLE)) {
            for (var shuttleBusType : List.of(ShuttleRouteType.SHUTTLE, WEEKEND)) {
                busRoutes.addAll(shuttleBusRepository.findAllByRouteType(shuttleBusType));
            }
        }

        LocalTime arrivalTime = busRoutes.stream()
            .filter(busRoute -> busRoute.getRegion().equals(CHEONAN_ASAN))
            .map(this::busRouteConvertor)
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
