package in.koreatech.koin.domain.bus.model;

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

    public static BusType from(String busType) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].name.equals(busType)) {
                return values()[i];
            }
        }
        throw BusStationNotFoundException.withDetail("busType: " + busType);
    }
}
