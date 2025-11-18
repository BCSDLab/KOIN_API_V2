package in.koreatech.koin.admin.bus.commuting.enums;

import in.koreatech.koin.domain.bus.exception.BusDirectionNotFoundException;
import lombok.Getter;

@Getter
public enum BusDirection {
    NORTH("등교"),
    SOUTH("하교"),
    BOTH("등하교"),
    ;

    private final String name;

    BusDirection(String name) {
        this.name = name;
    }

    public static BusDirection of(String name) {
        for (BusDirection direction : BusDirection.values()) {
            if (direction.name().equals(name)) {
                return direction;
            }
        }
        throw BusDirectionNotFoundException.withDetail("name: " + name);
    }

    public boolean isSouth() {
        return this == SOUTH;
    }

    public boolean isNotSouth() {
        return this == NORTH || this == BOTH;
    }

    public boolean isNotNorth() {
        return this == SOUTH || this == BOTH;
    }
}
