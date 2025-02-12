package in.koreatech.koin.domain.bus.enums;

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
    NORTH("등교", "to"),

    /**
     * 하행: 병천 -> 종합터미널 (천안역, 터미널행)
     */
    SOUTH("하교", "from"),
    ;

    private final String name;
    private final String direct;

    public static BusDirection from(String directions) {
        for (BusDirection direction : values()) {
            if (StringUtils.equals(direction.name, directions)) {
                return direction;
            }
        }
        throw BusDirectionNotFoundException.withDetail("directions: " + directions);
    }

    public static boolean isReverseDirection(String direction) {
        return StringUtils.equals(direction, "하교");
    }
}
