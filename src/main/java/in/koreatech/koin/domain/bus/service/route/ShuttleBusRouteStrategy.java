package in.koreatech.koin.domain.bus.service.route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.service.ShuttleBusService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShuttleBusRouteStrategy implements BusRouteStrategy {

    private static final Map<BusStation, Set<BusStation>> VALID_ROUTES = createValidRoutes();
    private final ShuttleBusService shuttleBusService;

    @Override
    public List<ScheduleInfo> findSchedule(BusRouteCommand command) {
        if (!isValidRoute(command.depart(), command.arrive())) {
            return Collections.emptyList();
        }

        List<ScheduleInfo> schedules = new ArrayList<>();
        schedules.addAll(shuttleBusService.getShuttleBusSchedule(command, BusType.SHUTTLE));
        schedules.addAll(shuttleBusService.getShuttleBusSchedule(command, BusType.COMMUTING));
        return schedules;
    }

    @Override
    public boolean support(BusRouteType type) {
        return type == BusRouteType.SHUTTLE || type == BusRouteType.ALL;
    }

    private boolean isValidRoute(BusStation depart, BusStation arrive) {
        return VALID_ROUTES.getOrDefault(depart, Collections.emptySet()).contains(arrive);
    }

    private static Map<BusStation, Set<BusStation>> createValidRoutes() {
        Map<BusStation, Set<BusStation>> routes = new EnumMap<>(BusStation.class);

        routes.put(BusStation.KOREATECH, EnumSet.of(BusStation.TERMINAL, BusStation.STATION));
        routes.put(BusStation.TERMINAL, EnumSet.of(BusStation.KOREATECH));
        routes.put(BusStation.STATION, EnumSet.of(BusStation.KOREATECH));

        return Collections.unmodifiableMap(routes);
    }
}
