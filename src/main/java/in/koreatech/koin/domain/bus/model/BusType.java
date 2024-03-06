package in.koreatech.koin.domain.bus.model;

import java.util.Arrays;

import in.koreatech.koin.domain.bus.exception.BusStationNotFoundException;
import lombok.Getter;

@Getter
public enum BusType {
    CITY("city"),
    EXPRESS("express"),
    SHUTTLE("shuttle"),
    COMMUTING("commuting"),
    ;

    private String name;

    BusType(String name) {
        this.name = name;
    }

    public static BusType from(String busTypeName) {
        return Arrays.stream(values())
            .filter(busType -> busType.name.equals(busTypeName))
            .findAny()
            .orElseThrow(() -> BusStationNotFoundException.withDetail("busType: " + busTypeName));
    }
}
