package in.koreatech.koin.domain.bus.model.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.domain.bus.exception.BusTypeNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CityBusDirection {
    종합터미널("코리아텍", "천안 터미널"),
    병천3리("천안 터미널", "코리아텍"),
    황사동("천안 터미널", "코리아텍"),
    유관순열사사적지("천안 터미널", "코리아텍");

    private final String departNode;
    private final String apartNode;

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
