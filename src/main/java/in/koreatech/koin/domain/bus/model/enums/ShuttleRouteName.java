package in.koreatech.koin.domain.bus.model.enums;

import static in.koreatech.koin.domain.bus.model.enums.ShuttleBusRegion.*;
import static in.koreatech.koin.domain.bus.model.enums.ShuttleRouteType.*;

import java.util.List;

import in.koreatech.koin.domain.bus.exception.BusIllegalRegionException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShuttleRouteName {
    CHEONAN_COMMUTING("천안", CHEONAN_ASAN, List.of(WEEKDAYS)),
    CHEONGJU_COMMUTING("청주", CHEONGJU, List.of(WEEKDAYS)),
    SEOUL_COMMUTING("서울", SEOUL, List.of(WEEKDAYS)),
    DAEJEON_COMMUTING("대전", DAEJEON_SEJONG, List.of(WEEKDAYS)),
    SEJONG_COMMUTING("세종", DAEJEON_SEJONG, List.of(WEEKDAYS)),

    CHEONAN_SHUTTLE("천안", CHEONAN_ASAN, List.of(SHUTTLE, WEEKEND)),
    CHEONGJU_SHUTTLE("청주", CHEONGJU, List.of(SHUTTLE));

    private final String regionName;
    private final ShuttleBusRegion busRegion;
    private final List<ShuttleRouteType> routeTypes;

    public static ShuttleRouteName getOrdinalByLabel(String regionName) {
        for (ShuttleRouteName region : ShuttleRouteName.values()) {
            if (region.getRegionName().equals(regionName)) {
                return region;
            }
        }
        throw BusIllegalRegionException.withDetail("displayName: " + regionName);
    }
}
