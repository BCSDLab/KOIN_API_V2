package in.koreatech.koin.domain.bus.global.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import in.koreatech.koin.domain.bus.global.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.shuttle.model.enums.BusStation;

public record BusRouteCommand (

    BusStation depart,
    BusStation arrive,
    BusRouteType busRouteType,
    LocalDate date,
    LocalTime time
) {


}
