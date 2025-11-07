package in.koreatech.koin.domain.bus.enums;

import in.koreatech.koin.domain.bus.exception.BusIllegalRegionException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShuttleBusRegion {
    CHEONAN_ASAN("천안・아산"),
    CHEONGJU("청주"),
    SEOUL("서울"),
    DAEJEON_SEJONG("대전・세종"),
    ;

    private final String label;

    public static int getOrdinalByLabel(String label) {
        for (var region : ShuttleBusRegion.values()) {
            if (region.getLabel().equals(label)) {
                return region.ordinal();
            }
        }
        throw BusIllegalRegionException.withDetail("displayName: " + label);
    }

    public static ShuttleBusRegion convertFrom(String label) {
        for (var region : ShuttleBusRegion.values()) {
            if (region.getLabel().contains(label)) {
                return region;
            }
        }
        throw BusIllegalRegionException.withDetail("displayName: " + label);
    }

    public static boolean isValid(String label) {
        for (var region : ShuttleBusRegion.values()) {
            if (region.getLabel().contains(label)) {
                return true;
            }
        }
        throw BusIllegalRegionException.withDetail("displayName: " + label);
    }
}
