package in.koreatech.koin.domain.bus.enums;

import in.koreatech.koin.domain.bus.exception.BusIllegalRouteTypeException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShuttleRouteType {
    SHUTTLE("순환"),
    WEEKDAYS("주중"),
    WEEKEND("주말"),
    ;

    private final String label;

    public static int getOrdinalByLabel(String label) {
        for (ShuttleRouteType shuttleRouteType : ShuttleRouteType.values()) {
            if (shuttleRouteType.getLabel().equals(label)) {
                return shuttleRouteType.ordinal();
            }
        }
        throw BusIllegalRouteTypeException.withDetail("displayName: " + label);
    }
}
