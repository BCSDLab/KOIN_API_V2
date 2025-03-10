package in.koreatech.koin.domain.bus.enums;

import in.koreatech.koin.domain.bus.exception.BusDirectionNotFoundException;
import lombok.Getter;

@Getter
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
    private final String legacyDirection;

    BusDirection(String name, String legacyDirection) {
        this.name = name;
        this.legacyDirection = legacyDirection;
    }

    // Deprecated: 강제 업데이트 이후 삭제할 레거시 Bus
    public static BusDirection from(String legacyDirection) {
        for (var direction : BusDirection.values()) {
            if (direction.getLegacyDirection().equals(legacyDirection)) {
                return direction;
            }
        }
        throw BusDirectionNotFoundException.withDetail("direction: " + legacyDirection);
    }
}
