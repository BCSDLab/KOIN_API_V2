package in.koreatech.koin.domain.bus.model.enums;

import java.util.Arrays;

import in.koreatech.koin.domain.bus.exception.ApiTypeNotFoundException;
import lombok.Getter;

@Getter
public enum BusApiType {
    CITY("cityBusOpenApiClient"),
    EXPRESS("intercityBusOpenApiClient")
    ;

    private final String value;

    BusApiType(String bean) {
        this.value = bean;
    }

    public static BusApiType from(BusType value) {
        return Arrays.stream(values())
            .filter(busApiType -> busApiType.toString().equalsIgnoreCase(value.toString()))
            .findAny()
            .orElseThrow(() -> ApiTypeNotFoundException.withDetail("apiType: " + value));
    }
}
