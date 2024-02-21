package in.koreatech.koin.domain.bus.model;

import in.koreatech.koin.domain.bus.exception.BusStationNotFoundException;

public enum BusStation {
    KOREATECH("koreatech"),
    STATION("station"),
    TERMINAL("terminal"),
    ;

    private final String name;

    BusStation(String name) {
        this.name = name;
    }

    public static BusStation from(String busStation) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].name.equals(busStation)) {
                return values()[i];
            }
        }
        throw BusStationNotFoundException.withDetail("busStation: " + busStation);
    }
}
