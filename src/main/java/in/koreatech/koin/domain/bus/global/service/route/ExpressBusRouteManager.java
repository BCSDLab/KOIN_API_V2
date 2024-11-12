package in.koreatech.koin.domain.bus.global.service.route;

import static in.koreatech.koin.domain.bus.global.dto.BusScheduleResponse.ScheduleInfo.toScheduleInfo;

import java.time.LocalTime;
import java.util.List;


import in.koreatech.koin.domain.bus.global.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.city.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.express.model.ExpressBusSchedule;

public final class ExpressBusRouteManager {

    private static final String BUS_TYPE = "express";
    private static final String BUS_NAME = "대성티엔이";

    public static List<ScheduleInfo> getExpressBusSchedule(BusDirection direction) {
        List<LocalTime> departTimes = getStaticExpressBusScheduleTimeList(direction);
        return toScheduleInfo(departTimes, BUS_TYPE, BUS_NAME);
    }

    private static List<LocalTime> getStaticExpressBusScheduleTimeList(BusDirection direction) {
        return switch (direction) {
            case NORTH -> ExpressBusSchedule.getExpressBusScheduleToKoreaTech();
            case SOUTH -> ExpressBusSchedule.getExpressBusScheduleToTerminal();
        };
    }
}
