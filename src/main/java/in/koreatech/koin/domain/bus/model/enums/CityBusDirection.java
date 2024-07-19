package in.koreatech.koin.domain.bus.model.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.domain.bus.exception.BusTypeNotFoundException;

public enum CityBusDirection {
    종합터미널,
    병천3리,
    황사동,
    유관순열사사적지
    ;

    @JsonCreator
    public static CityBusDirection from(String direction) {
        return Arrays.stream(values())
            .filter(direct -> direct.name().equalsIgnoreCase(direction))
            .findAny()
            .orElseThrow(() -> BusTypeNotFoundException.withDetail("busDirection: " + direction));
    }

    public String getName() {
        return this.name().toLowerCase();
    }
}
