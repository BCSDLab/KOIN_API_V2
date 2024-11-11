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
        List<Route> shuttleRoutesDirectionFrom = busCourseDirectionFrom.getBusRoutesByDate(date);
        List<Route> shuttleRoutesDirectionTo = busCourseDirectionTo.getBusRoutesByDate(date);

        switch (depart) {
            case KOREATECH -> {
                return getShuttleRoutesDepartKoreaTech(arrive, shuttleRoutesDirectionFrom);
            }
            case STATION -> {
                return getShuttleRoutesDepartStation(arrive, shuttleRoutesDirectionFrom, shuttleRoutesDirectionTo);
            }
            case TERMINAL -> {
                return getShuttleRoutesDepartTerminal(arrive, shuttleRoutesDirectionFrom, shuttleRoutesDirectionTo);
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    public static List<ScheduleInfo> getCommutingBusSchedule(
            BusCourse busCourseDirectionFrom,
            BusCourse busCourseDirectionTo,
            BusStation depart,
            BusStation arrive,
            LocalDate date
    ) {
        List<Route> shuttleRoutesDirectionFrom = busCourseDirectionFrom.getBusRoutesByDate(date);
        List<Route> shuttleRoutesDirectionTo = busCourseDirectionTo.getBusRoutesByDate(date);

        switch (depart) {
            case KOREATECH -> {
                return getCommutingBusRoutesDepartKoreaTech(arrive, shuttleRoutesDirectionFrom);
            }
            case STATION -> {
                return getCommutingBusRoutesDepartStation(arrive, shuttleRoutesDirectionTo);
            }
            case TERMINAL -> {
                return getCommutingBusRoutesDepartTerminal(arrive, shuttleRoutesDirectionTo);
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    private static List<ScheduleInfo> getShuttleRoutesDepartKoreaTech(BusStation arrive, List<Route> shuttleRoutes) {
        return switch (arrive) {
            case TERMINAL -> getScheduleInfoDepartKoreaTech(shuttleRoutes, TERMINAL_NODE_NAME);
            case STATION -> getScheduleInfoDepartKoreaTech(shuttleRoutes, STATION_NODE_NAME);
            default -> Collections.emptyList();
        };
    }

    private static List<ScheduleInfo> getShuttleRoutesDepartTerminal(
            BusStation arrive,
            List<Route> shuttleRoutesDirectionFrom,
            List<Route> shuttleRoutesDirectionTo
    ) {
        switch (arrive) {
            case KOREATECH -> {
                List<ScheduleInfo> scheduleInfo = new ArrayList<>();
                scheduleInfo.addAll(getScheduleInfoForCircularRoute(shuttleRoutesDirectionFrom, TERMINAL_NODE_NAME));
                scheduleInfo.addAll(getScheduleInfoDepartElse(shuttleRoutesDirectionTo, TERMINAL_NODE_NAME));
                return scheduleInfo;
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    private static List<ScheduleInfo> getShuttleRoutesDepartStation(
            BusStation arrive,
            List<Route> shuttleRoutesDirectionFrom,
            List<Route> shuttleRoutesDirectionTo
    ) {
        switch (arrive) {
            case KOREATECH -> {
                List<ScheduleInfo> scheduleInfo = new ArrayList<>();
                scheduleInfo.addAll(getScheduleInfoForCircularRoute(shuttleRoutesDirectionFrom, STATION_NODE_NAME));
                scheduleInfo.addAll(getScheduleInfoDepartElse(shuttleRoutesDirectionTo, STATION_NODE_NAME));
                return scheduleInfo;
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    public static List<ScheduleInfo> getCommutingBusRoutesDepartKoreaTech(BusStation arrive, List<Route> shuttleRoutes) {
        switch (arrive) {
            case TERMINAL -> {
                return getScheduleInfoDepartKoreaTech(shuttleRoutes, TERMINAL_NODE_NAME, COMMUTING_BUS_ROUTE_DOWN);
            }
            case STATION -> {
                return getScheduleInfoDepartKoreaTech(shuttleRoutes, STATION_NODE_NAME, COMMUTING_BUS_ROUTE_DOWN);
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    public static List<ScheduleInfo> getCommutingBusRoutesDepartTerminal(BusStation arrive, List<Route> shuttleRoutes) {
        switch (arrive) {
            case KOREATECH -> {
                return getScheduleInfoDepartElse(shuttleRoutes, TERMINAL_NODE_NAME, COMMUTING_BUS_ROUTE_UP);
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    public static List<ScheduleInfo> getCommutingBusRoutesDepartStation(BusStation arrive, List<Route> shuttleRoutes) {
        switch (arrive) {
            case KOREATECH -> {
                return getScheduleInfoDepartElse(shuttleRoutes, STATION_NODE_NAME, COMMUTING_BUS_ROUTE_UP);
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    private static List<ScheduleInfo> getScheduleInfoDepartKoreaTech(List<Route> shuttleRoutes, String arrivalNodeName) {
        return getScheduleInfoDepartKoreaTech(shuttleRoutes, arrivalNodeName, "");
    }

    private static List<ScheduleInfo> getScheduleInfoDepartKoreaTech(List<Route> shuttleRoutes, String arrivalNodeName, String routeName) {
        return shuttleRoutes.stream()
                .map(route -> {
                    Route.ArrivalNode arrivalNode = route.checkContainNode(arrivalNodeName);
                    Route.ArrivalNode koreaTechNode = route.checkContainNode(KOREATECH_NODE_NAME);

                    if (arrivalNode != null && koreaTechNode != null) {
                        return new ScheduleInfo(
                                BUS_TYPE,
                                String.join(" ", route.getRouteName(), routeName),
                                LocalTime.parse(koreaTechNode.getArrivalTime()));
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static List<ScheduleInfo> getScheduleInfoDepartElse(List<Route> shuttleRoutes, String arrivalNodeName) {
        return getScheduleInfoDepartElse(shuttleRoutes, arrivalNodeName, "");
    }

    private static List<ScheduleInfo> getScheduleInfoDepartElse(List<Route> shuttleRoutes, String arrivalNodeName, String routeName) {
        return shuttleRoutes.stream()
                .map(route -> {
                    ArrivalNode arrivalNode = route.checkContainNode(arrivalNodeName);
                    ArrivalNode koreaTechNode = route.checkContainNode(KOREATECH_NODE_NAME);

                    if (arrivalNode != null && koreaTechNode != null) {
                        return new ScheduleInfo(
                                BUS_TYPE,
                                String.join(" ", route.getRouteName(), routeName),
                                LocalTime.parse(arrivalNode.getArrivalTime()));
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static List<ScheduleInfo> getScheduleInfoForCircularRoute(List<Route> shuttleRoutes, String arrivalNodeName) {
        return shuttleRoutes.stream()
                .map(route -> {
                    ArrivalNode arrivalNode = route.checkContainNode(arrivalNodeName);
                    List<ArrivalNode> koreaTechNode = route.checkContainNodes(KOREATECH_NODE_NAME);

                    if (koreaTechNode.size() == 2 && arrivalNode != null) {
                        return new ScheduleInfo(BUS_TYPE, route.getRouteName(), LocalTime.parse(arrivalNode.getArrivalTime()));
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
