package in.koreatech.koin.domain.bus.service.route;

import static in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo.toScheduleInfo;

import java.time.LocalTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.express.ExpressBusRouteSchedule;

@Component
public class ExpressBusRouteStrategy implements BusRouteStrategy{

    private static final Map<BusStation, Set<BusStation>> VALID_ROUTES = createValidRoutes();
    private static final String BUS_TYPE = "express";
    private static final String BUS_NAME = "대성티엔이";

    @Override
    public List<ScheduleInfo> findSchedule(BusRouteCommand command) {
        if (!isValidRoute(command.depart(), command.arrive())) {
            return Collections.emptyList();
        }

        BusDirection direction = getRouteDirection(command.depart(), command.arrive());
        List<LocalTime> departTimes = getStaticExpressBusScheduleTimeList(direction);
        return toScheduleInfo(departTimes, BUS_TYPE, BUS_NAME);
    }

    @Override
    public boolean support(BusRouteType type) {
        return type == BusRouteType.EXPRESS || type == BusRouteType.ALL;
    }

    private static List<LocalTime> getStaticExpressBusScheduleTimeList(BusDirection direction) {
        return switch (direction) {
            case NORTH -> ExpressBusRouteSchedule.getExpressBusScheduleToKoreaTech();
            case SOUTH -> ExpressBusRouteSchedule.getExpressBusScheduleToTerminal();
        };
    }

    private BusDirection getRouteDirection(BusStation depart, BusStation arrive) {
        return (depart == BusStation.KOREATECH && arrive == BusStation.TERMINAL)
            ? BusDirection.NORTH : BusDirection.SOUTH;
    }

    private boolean isValidRoute(BusStation depart, BusStation arrive) {
        return VALID_ROUTES.getOrDefault(depart, Collections.emptySet()).contains(arrive);
    }

    private static Map<BusStation, Set<BusStation>> createValidRoutes() {
        Map<BusStation, Set<BusStation>> routes = new EnumMap<>(BusStation.class);

        routes.put(BusStation.KOREATECH, EnumSet.of(BusStation.TERMINAL));
        routes.put(BusStation.TERMINAL, EnumSet.of(BusStation.KOREATECH));

        return Collections.unmodifiableMap(routes);
    }
}
