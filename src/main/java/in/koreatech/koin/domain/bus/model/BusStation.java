package in.koreatech.koin.domain.bus.model;

import java.util.Arrays;
import java.util.List;

import in.koreatech.koin.domain.bus.exception.BusStationNotFoundException;
import lombok.Getter;

@Getter
public enum BusStation {
    KOREATECH(List.of("학교", "한기대")),
    STATION(List.of("천안역", "천안역(학화호두과자)")),
    TERMINAL(List.of("터미널", "터미널(신세계 앞 횡단보도)")),
    ;

    private final List<String> displayNames;

    BusStation(List<String> displayNames) {
        this.displayNames = displayNames;
    }

    public static BusStation from(String busStationName) {
        return Arrays.stream(values())
            .filter(busStation -> busStation.name().equalsIgnoreCase(busStationName))
            .findAny()
            .orElseThrow(() -> BusStationNotFoundException.withDetail("busStation: " + busStationName));
    }
}
