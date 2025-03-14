package in.koreatech.koin.domain.bus.service.model.route;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.enums.BusDirection;
import in.koreatech.koin.domain.bus.enums.BusRouteType;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusSchedule;

@Component
public class ExpressBusRouteStrategy implements BusRouteStrategy {

    private static final String BUS_TYPE = "express";
    private static final String BUS_NAME = "대성티엔이";

    @Override
    public List<ScheduleInfo> findSchedule(BusRouteCommand command) {
        if(validRoute(command.depart(), command.arrive())) return Collections.emptyList();
        BusDirection direction = getRouteDirection(command.depart(), command.arrive());

        return getStaticExpressBusScheduleTimeList(direction).stream()
            .map(time -> new ScheduleInfo(BUS_TYPE, BUS_NAME, time))
            .toList();
    }

    @Override
    public boolean support(BusRouteType type) {
        return type == BusRouteType.EXPRESS || type == BusRouteType.ALL;
    }

    private List<LocalTime> getStaticExpressBusScheduleTimeList(BusDirection direction) {
        return switch (direction) {
            case NORTH -> ExpressBusSchedule.KoreaTechToTerminal();
            case SOUTH -> ExpressBusSchedule.TerminalToKoreaTech();
        };
    }

    private BusDirection getRouteDirection(BusStation depart, BusStation arrive) {
        return (depart == BusStation.KOREATECH && arrive == BusStation.TERMINAL)
            ? BusDirection.NORTH : BusDirection.SOUTH;
    }

    private boolean validRoute(BusStation depart, BusStation arrive) {
        return (depart == BusStation.STATION || arrive == BusStation.STATION);
    }
}
