package in.koreatech.koin.domain.bus.enums;

import org.apache.commons.lang3.StringUtils;

import in.koreatech.koin.domain.bus.exception.BusDirectionNotFoundException;
import lombok.Getter;

@Getter
public enum BusDirection {
    /**
     * 상행: 종합터미널 -> 병천 (한기대행)
     */
    NORTH("등교"),

    /**
     * 하행: 병천 -> 종합터미널 (천안역, 터미널행)
     */
    SOUTH("하교"),
    ;

    private final String name;

    BusDirection(String name) {
        this.name = name;
    }

    public static BusDirection from(String direction) {
        if (StringUtils.equals(direction, "to")) {
            return NORTH;
        }
        if (StringUtils.equals(direction, "from")) {
            return SOUTH;
        }
        throw BusDirectionNotFoundException.withDetail("direction: " + direction);
    }
}
