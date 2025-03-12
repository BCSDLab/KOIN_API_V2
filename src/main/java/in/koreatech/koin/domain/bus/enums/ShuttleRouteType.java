package in.koreatech.koin.domain.bus.enums;

import in.koreatech.koin.domain.bus.exception.BusIllegalRouteTypeException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum ShuttleRouteType {
    SHUTTLE("순환", BusType.SHUTTLE),
    WEEKDAYS("주중", BusType.COMMUTING),
    WEEKEND("주말", BusType.SHUTTLE),
    ;

    private final String label;
    private final BusType legacyBusType;

    public static int getOrdinalByLabel(String label) {
        for (ShuttleRouteType shuttleRouteType : ShuttleRouteType.values()) {
            if (shuttleRouteType.getLabel().equals(label)) {
                return shuttleRouteType.ordinal();
            }
        }
        throw BusIllegalRouteTypeException.withDetail("displayName: " + label);
    }

    // Deprecated: 강제 업데이트 이후 삭제할 레거시 Bus
    public static List<ShuttleRouteType> convertFrom(BusType legacyBusType) {
        List<ShuttleRouteType> newTypes = new ArrayList<>();
        for (var shuttleRouteType : ShuttleRouteType.values()) {
            if (Objects.equals(shuttleRouteType.getLegacyBusType(), legacyBusType)) {
                newTypes.add(shuttleRouteType);
            }
        }
        return newTypes;
    }
}
