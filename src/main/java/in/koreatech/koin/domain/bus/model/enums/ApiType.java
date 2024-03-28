package in.koreatech.koin.domain.bus.model.enums;

import java.util.Arrays;

import in.koreatech.koin.domain.bus.exception.ApiTypeNotFoundException;
import lombok.Getter;

@Getter
public enum ApiType {
    CITY("cityBusOpenApiRequester"),
    EXPRESS("intercityBusOpenApiRequester")
    ;

    private final String value;

    ApiType(String bean) {
        this.value = bean;
    }

    public static ApiType from(BusType value) {
        return Arrays.stream(values())
            .filter(apiType -> apiType.toString().equalsIgnoreCase(value.toString()))
            .findAny()
            .orElseThrow(() -> ApiTypeNotFoundException.withDetail("apiType: " + value));
    }
}
