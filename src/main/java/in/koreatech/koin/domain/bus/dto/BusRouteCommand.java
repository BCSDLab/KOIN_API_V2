package in.koreatech.koin.domain.bus.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import in.koreatech.koin.domain.bus.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.model.enums.BusStation;

public record BusRouteCommand(

    BusStation depart,
    BusStation arrive,
    BusRouteType busRouteType,
    LocalDate date,
    LocalTime time
) {

}
