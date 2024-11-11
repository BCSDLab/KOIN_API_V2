package in.koreatech.koin.domain.bus.city.model.enums;

import org.apache.commons.lang3.StringUtils;

import in.koreatech.koin.domain.bus.global.exception.BusDirectionNotFoundException;

public enum BusDirection {
    /**
     * 상행: 종합터미널 -> 병천 (한기대행)
     */
    NORTH,

    /**
     * 하행: 병천 -> 종합터미널 (천안역, 터미널행)
     */
    SOUTH,
    ;

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
