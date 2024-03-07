package in.koreatech.koin.domain.bus.model;

import java.util.Arrays;

import in.koreatech.koin.domain.bus.exception.BusStationNotFoundException;
import lombok.Getter;

@Getter
public enum BusType {
    CITY,
    EXPRESS,
    SHUTTLE,
    COMMUTING,
    ;

    public static BusType from(String busTypeName) {
        return Arrays.stream(values())
            .filter(busType -> busType.name().equalsIgnoreCase(busTypeName))
            .findAny()
            .orElseThrow(() -> BusStationNotFoundException.withDetail("busType: " + busTypeName));
    }
}
