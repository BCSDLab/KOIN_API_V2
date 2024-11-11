package in.koreatech.koin.domain.bus.global.model.enums;

import java.util.Arrays;

import in.koreatech.koin.domain.bus.global.exception.ApiTypeNotFoundException;
import lombok.Getter;

@Getter
public enum BusApiType {
    CITY,
    EXPRESS,
    ;

    public static BusApiType from(BusType value) {
        return Arrays.stream(values())
            .filter(busApiType -> busApiType.toString().equalsIgnoreCase(value.toString()))
            .findAny()
            .orElseThrow(() -> ApiTypeNotFoundException.withDetail("apiType: " + value));
    }
}
