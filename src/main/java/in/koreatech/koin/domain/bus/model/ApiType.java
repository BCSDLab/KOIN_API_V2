package in.koreatech.koin.domain.bus.model;

import java.util.Arrays;

import in.koreatech.koin.domain.bus.exception.ApiTypeNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiType {
    CITY("cityBusOpenApiRequester"), EXPRESS("intercityBusOpenApiRequester");

    private final String value;

    public static ApiType from(BusType value) {
        return Arrays.stream(values())
            .filter(apiType -> apiType.toString().equalsIgnoreCase(value.toString()))
            .findAny()
            .orElseThrow(() -> ApiTypeNotFoundException.withDetail("apiType: " + value));
    }
}
