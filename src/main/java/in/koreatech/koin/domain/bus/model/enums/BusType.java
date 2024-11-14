package in.koreatech.koin.domain.bus.model.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.domain.bus.exception.BusTypeNotFoundException;
import lombok.Getter;

@Getter
public enum BusType {
    CITY("시내버스"),
    EXPRESS("대성고속"),
    SHUTTLE("셔틀버스"),
    COMMUTING("통학버스"),
    ;

    private final String label;

    BusType(String label) {
        this.label = label;
    }

    @JsonCreator
    public static BusType from(String busTypeName) {
        return Arrays.stream(values())
            .filter(busType -> busType.name().equalsIgnoreCase(busTypeName))
            .findAny()
            .orElseThrow(() -> BusTypeNotFoundException.withDetail("busType: " + busTypeName));
    }

    public String getName() {
        return this.name().toLowerCase();
    }
}
