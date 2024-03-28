package in.koreatech.koin.domain.bus.model.enums;

import java.util.Arrays;

import in.koreatech.koin.domain.bus.exception.BusTypeNotFoundException;
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
            .orElseThrow(() -> BusTypeNotFoundException.withDetail("busType: " + busTypeName));
    }
}
