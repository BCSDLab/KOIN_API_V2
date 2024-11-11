package in.koreatech.koin.domain.bus.global.model.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.domain.bus.global.exception.BusTypeNotFoundException;

public enum BusRouteType {
    CITY,
    EXPRESS,
    SHUTTLE,
    ALL;

    @JsonCreator
    public static BusRouteType from(String busRouteTypeName) {
        return Arrays.stream(values())
            .filter(busType -> busType.name().equalsIgnoreCase(busRouteTypeName))
            .findAny()
            .orElseThrow(() -> BusTypeNotFoundException.withDetail("busRouteTypeName: " + busRouteTypeName));
    }

    public String getName() {
        return this.name().toLowerCase();
    }
}
