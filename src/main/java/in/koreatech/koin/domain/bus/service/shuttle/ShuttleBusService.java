package in.koreatech.koin.domain.bus.service.shuttle;

import static in.koreatech.koin.domain.bus.model.enums.BusType.COMMUTING;
import static in.koreatech.koin.domain.bus.model.enums.BusType.SHUTTLE;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Collection;
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
import in.koreatech.koin.domain.bus.service.model.BusRemainTime;
import in.koreatech.koin.domain.bus.service.shuttle.model.SchoolBusTimetable;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.BusType;
import in.koreatech.koin.domain.bus.service.shuttle.model.BusCourse;
import in.koreatech.koin.domain.bus.service.shuttle.model.Route;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.SchoolBusTimetable;
import in.koreatech.koin.domain.bus.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.model.enums.ShuttleRouteName;
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

    public List<BusCourseResponse> getBusCourses() {
        return busRepository.findAll().stream()
            .map(BusCourseResponse::from)
            .toList();
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
        List<SchoolBusTimetable> timetableList = new ArrayList<>();

        // 기존 요청으로 최신 db에 맞는 요청으로 변환
        ShuttleRouteName routeName = ShuttleRouteName.getRegionByLegacy(busType, region);

        // ex) 통학-천안 다 가져오기
        List<ShuttleBusRoute> routes = routeName.getRouteTypes().stream()
            .map(routeType -> shuttleBusRepository.findAllByRegionAndRouteTypeAndSemesterType(
                routeName.getBusRegion(), routeType, "방학기간"))
            .flatMap(List::stream)
            .toList();

        // to -> 하교 / from -> 등교 변환
        BusDirection busDirection = BusDirection.from(direction);

        // 등교/하교 필터링 필요
        for (var route : routes) {
            List<String> nodeInfo = route.getNodeInfo().stream()
                .map(ShuttleBusRoute.NodeInfo::getName)
                .toList();
            List<ShuttleBusRoute.RouteInfo> routeInfos = new ArrayList<>();

            if (busType.equals(COMMUTING)) {
                routeInfos.addAll(route.getRouteInfo().stream()
                    .filter(direct -> direct.getName().equals(busDirection.getName()))
                    .toList());
            }
            if (busType.equals(SHUTTLE)) {
                if (route.getRouteName().contains("일학습병행대학")) {
                    routeInfos.addAll(route.getRouteInfo().stream()
                        .filter(direct -> direct.getName().equals(busDirection.getName()))
                        .toList());
                } else {
                    routeInfos.addAll(route.getRouteInfo().stream()
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
                timetableList.add(new SchoolBusTimetable(route.getRouteName(), arrivalNodes));
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
