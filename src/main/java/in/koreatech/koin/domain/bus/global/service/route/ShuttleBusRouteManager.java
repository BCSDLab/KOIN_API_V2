package in.koreatech.koin.domain.bus.global.service.route;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import in.koreatech.koin.domain.bus.global.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.shuttle.model.enums.BusStation;
import in.koreatech.koin.domain.bus.shuttle.model.BusCourse;
import in.koreatech.koin.domain.bus.shuttle.model.Route;
import in.koreatech.koin.domain.bus.shuttle.model.Route.ArrivalNode;

public final class ShuttleBusRouteManager {

    private static final String BUS_TYPE = "shuttle";
    private static final String KOREATECH_NODE_NAME = "한기대";
    private static final String TERMINAL_NODE_NAME = "터미널";
    private static final String STATION_NODE_NAME = "천안역";
    private static final String COMMUTING_BUS_ROUTE_UP = "등교셔틀";
    private static final String COMMUTING_BUS_ROUTE_DOWN = "하교셔틀";

    public static List<ScheduleInfo> getShuttleBusSchedule(
        BusCourse busCourseDirectionFrom,
        BusCourse busCourseDirectionTo,
        BusStation depart,
        BusStation arrive,
        LocalDate date
    ) {
        List<Route> fromRoutes = busCourseDirectionFrom.getBusRoutesByDate(date);
        List<Route> toRoutes = busCourseDirectionTo.getBusRoutesByDate(date);

        return switch (depart) {
            case KOREATECH -> getShuttleRoutesDepartKoreaTech(arrive, fromRoutes);
            case STATION -> getShuttleRoutesArriveKoreaTech(fromRoutes, toRoutes, STATION_NODE_NAME);
            case TERMINAL -> getShuttleRoutesArriveKoreaTech(fromRoutes, toRoutes, TERMINAL_NODE_NAME);
        };
    }

    public static List<ScheduleInfo> getCommutingBusSchedule(
        BusCourse busCourseDirectionFrom,
        BusCourse busCourseDirectionTo,
        BusStation depart,
        BusStation arrive,
        LocalDate date
    ) {
        List<Route> fromRoutes = busCourseDirectionFrom.getBusRoutesByDate(date);
        List<Route> toRoutes = busCourseDirectionTo.getBusRoutesByDate(date);

        return switch (depart) {
            case KOREATECH -> getCommutingBusRoutesDepartKoreaTech(arrive, fromRoutes);
            case STATION -> getCommutingBusRoutesArriveKoreaTech(toRoutes, STATION_NODE_NAME);
            case TERMINAL -> getCommutingBusRoutesArriveKoreaTech(toRoutes, TERMINAL_NODE_NAME);
        };
    }

    private static List<ScheduleInfo> getShuttleRoutesDepartKoreaTech(BusStation arrive, List<Route> shuttleRoutes) {
        return switch (arrive) {
            case TERMINAL -> getScheduleInfoDepartKoreaTech(shuttleRoutes, TERMINAL_NODE_NAME, "");
            case STATION -> getScheduleInfoDepartKoreaTech(shuttleRoutes, STATION_NODE_NAME, "");
            default -> Collections.emptyList();
        };
    }

    private static List<ScheduleInfo> getShuttleRoutesArriveKoreaTech(
        List<Route> routeFrom,
        List<Route> routeTo,
        String departNodeName
    ) {
        List<ScheduleInfo> scheduleInfo = new ArrayList<>();
        scheduleInfo.addAll(getScheduleInfoForCircularRoute(routeFrom, departNodeName));
        scheduleInfo.addAll(getScheduleInfoDepartElse(routeTo, departNodeName, ""));
        return scheduleInfo;
    }

    public static List<ScheduleInfo> getCommutingBusRoutesDepartKoreaTech(BusStation arrive,
        List<Route> shuttleRoutes) {
        return switch (arrive) {
            case TERMINAL ->
                getScheduleInfoDepartKoreaTech(shuttleRoutes, TERMINAL_NODE_NAME, COMMUTING_BUS_ROUTE_DOWN);
            case STATION -> getScheduleInfoDepartKoreaTech(shuttleRoutes, STATION_NODE_NAME, COMMUTING_BUS_ROUTE_DOWN);
            default -> Collections.emptyList();
        };
    }

    public static List<ScheduleInfo> getCommutingBusRoutesArriveKoreaTech(List<Route> shuttleRoutes,
        String departNodeName) {
        return getScheduleInfoDepartElse(shuttleRoutes, departNodeName, COMMUTING_BUS_ROUTE_UP);
    }

    private static List<ScheduleInfo> getScheduleInfoDepartKoreaTech(List<Route> shuttleRoutes, String arrivalNodeName,
        String routeName) {
        return shuttleRoutes.stream()
            .map(route -> {
                Route.ArrivalNode arrivalNode = route.checkContainNode(arrivalNodeName);
                Route.ArrivalNode koreaTechNode = route.checkContainNode(KOREATECH_NODE_NAME);

                if (arrivalNode != null && koreaTechNode != null) {
                    return new ScheduleInfo(
                        BUS_TYPE,
                        String.format("%s %s", route.getRouteName(), routeName),
                        LocalTime.parse(koreaTechNode.getArrivalTime()));
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private static List<ScheduleInfo> getScheduleInfoDepartElse(List<Route> shuttleRoutes, String arrivalNodeName,
        String routeName) {
        return shuttleRoutes.stream()
            .map(route -> {
                ArrivalNode arrivalNode = route.checkContainNode(arrivalNodeName);
                ArrivalNode koreaTechNode = route.checkContainNode(KOREATECH_NODE_NAME);

                if (arrivalNode != null && koreaTechNode != null) {
                    return new ScheduleInfo(
                        BUS_TYPE,
                        String.format("%s %s", route.getRouteName(), routeName),
                        LocalTime.parse(arrivalNode.getArrivalTime()));
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private static List<ScheduleInfo> getScheduleInfoForCircularRoute(List<Route> shuttleRoutes,
        String arrivalNodeName) {
        return shuttleRoutes.stream()
            .map(route -> {
                ArrivalNode arrivalNode = route.checkContainNode(arrivalNodeName);
                List<ArrivalNode> koreaTechNode = route.checkContainNodes(KOREATECH_NODE_NAME);

                if (koreaTechNode.size() == 2 && arrivalNode != null) {
                    return new ScheduleInfo(BUS_TYPE, route.getRouteName(),
                        LocalTime.parse(arrivalNode.getArrivalTime()));
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
