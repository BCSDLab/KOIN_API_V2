package in.koreatech.koin.domain.bus.enums;

import in.koreatech.koin.domain.bus.exception.BusIllegalRegionException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

import static in.koreatech.koin.domain.bus.enums.BusType.COMMUTING;
import static in.koreatech.koin.domain.bus.enums.BusType.SHUTTLE;

// Deprecated: 강제 업데이트 이후 삭제할 레거시 Bus
@Getter
@RequiredArgsConstructor
public enum ShuttleRouteName {
    COMMUTING_CHEONAN("천안", COMMUTING),
    COMMUTING_CHEONGJU("청주", COMMUTING),
    COMMUTING_SEOUL("서울", COMMUTING),
    COMMUTING_DAEJEON("대전", COMMUTING),
    COMMUTING_SEJONG("세종", COMMUTING),

    SHUTTLE_CHEONAN("천안", SHUTTLE),
    SHUTTLE_CHEONGJU("청주", SHUTTLE),
    ;

    private final String label;
    private final BusType busType;

    public static ShuttleRouteName of(BusType type, String regionName) {
        for (ShuttleRouteName region : values()) {
            if (Objects.equals(region.getBusType(), type)
                && Objects.equals(region.getLabel(), regionName)) {
                return region;
            }
        }
        throw BusIllegalRegionException.withDetail("displayName: " + regionName);
    }
}
