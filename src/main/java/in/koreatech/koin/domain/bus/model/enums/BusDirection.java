package in.koreatech.koin.domain.bus.model.enums;

import org.apache.commons.lang3.StringUtils;

import in.koreatech.koin.domain.bus.exception.BusDirectionNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusDirection {
    /**
     * 상행: 종합터미널 -> 병천 (한기대행)
     */
    NORTH("등교", true),

    /**
     * 하행: 병천 -> 종합터미널 (천안역, 터미널행)
     */
    SOUTH("하교", false),
    ;

    private final String name;
    private final boolean isNull;

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
