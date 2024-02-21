package in.koreatech.koin.domain.bus.model;

import java.util.List;

import in.koreatech.koin.domain.bus.exception.BusStationNotFoundException;
import lombok.Getter;

@Getter
public enum BusStation {
    KOREATECH("koreatech", List.of("학교", "한기대")),
    STATION("station", List.of("천안역", "천안역(학화호두과자)")),
    TERMINAL("terminal", List.of("터미널", "터미널(신세계 앞 횡단보도)")),
    ;

    private final String name;
    private final List<String> displayNames;

    BusStation(String name, List<String> displayNames) {
        this.name = name;
        this.displayNames = displayNames;
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
