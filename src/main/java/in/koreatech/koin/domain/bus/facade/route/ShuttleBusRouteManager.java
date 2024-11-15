package in.koreatech.koin.domain.bus.facade.route;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.shuttle.BusCourse;
import in.koreatech.koin.domain.bus.model.shuttle.Route;

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
        scheduleInfo.addAll(getScheduleInfoForCircularShuttle(routeFrom, departNodeName));
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
            .map(route -> route.getScheduleInfoForNormalShuttle(arrivalNodeName, KOREATECH_NODE_NAME, BUS_TYPE,
                routeName, true))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private static List<ScheduleInfo> getScheduleInfoDepartElse(List<Route> shuttleRoutes, String arrivalNodeName,
        String routeName) {
        return shuttleRoutes.stream()
            .map(route -> route.getScheduleInfoForNormalShuttle(arrivalNodeName, KOREATECH_NODE_NAME, BUS_TYPE,
                routeName, false))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private static List<ScheduleInfo> getScheduleInfoForCircularShuttle(List<Route> shuttleRoutes,
        String arrivalNodeName) {
        return shuttleRoutes.stream()
            .map(route -> route.getScheduleInfoForCircularShuttle(arrivalNodeName, KOREATECH_NODE_NAME, BUS_TYPE))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
