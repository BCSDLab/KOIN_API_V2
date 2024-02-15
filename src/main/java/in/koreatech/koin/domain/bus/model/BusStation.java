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

    public static String getDirection(String depart, String arrival) {
        int departIndex = getBusStationIndex(depart);
        int arrivalIndex = getBusStationIndex(arrival);
        if (departIndex < arrivalIndex) {
            return "from"; // 등교
        }
        return "to"; // 하교
    }

    private static int getBusStationIndex(String busStation) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].name.equals(busStation)) {
                return i;
            }
        }
        throw BusStationNotFoundException.withDetail("busStation: " + busStation);
    }
}
