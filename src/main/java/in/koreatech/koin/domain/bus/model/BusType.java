package in.koreatech.koin.domain.bus.model;

import in.koreatech.koin.domain.bus.exception.BusStationNotFoundException;

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

    public static void validate(String busType) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].name.equals(busType)) {
                return;
            }
        }
        throw BusStationNotFoundException.withDetail("busType: " + busType);
    }
}
