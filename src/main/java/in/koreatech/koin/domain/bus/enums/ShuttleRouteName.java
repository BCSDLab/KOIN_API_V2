package in.koreatech.koin.domain.bus.enums;

import static in.koreatech.koin.domain.bus.enums.BusType.COMMUTING;
import static in.koreatech.koin.domain.bus.enums.ShuttleBusRegion.*;
import static in.koreatech.koin.domain.bus.enums.ShuttleRouteType.*;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.domain.bus.exception.BusIllegalRegionException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShuttleRouteName {
    CHEONAN_COMMUTING("천안", COMMUTING, CHEONAN_ASAN, List.of(WEEKDAYS)),
    CHEONGJU_COMMUTING("청주", COMMUTING, CHEONGJU, List.of(WEEKDAYS)),
    SEOUL_COMMUTING("서울", COMMUTING, SEOUL, List.of(WEEKDAYS)),
    DAEJEON_COMMUTING("대전", COMMUTING, DAEJEON_SEJONG, List.of(WEEKDAYS)),
    SEJONG_COMMUTING("세종", COMMUTING, DAEJEON_SEJONG, List.of(WEEKDAYS)),

    CHEONAN_SHUTTLE("천안", BusType.SHUTTLE, CHEONAN_ASAN, List.of(SHUTTLE, WEEKEND)),
    CHEONGJU_SHUTTLE("청주", BusType.SHUTTLE, CHEONGJU, List.of(SHUTTLE));

    private final String regionName;
    private final BusType busType;
    private final ShuttleBusRegion busRegion;
    private final List<ShuttleRouteType> routeTypes;

    public static ShuttleRouteName getRegionByLegacy(BusType type, String regionName) {
        for (ShuttleRouteName region : ShuttleRouteName.values()) {
            if (region.getRegionName().equals(regionName) && region.getBusType().equals(type)) {
                return region;
            }
        }
        throw BusIllegalRegionException.withDetail("displayName: " + regionName);
    }

    public static List<ShuttleRouteName> getRoutesByLegacy(BusType type) {
        List<ShuttleRouteName> routes = new ArrayList<>();
        for (ShuttleRouteName region : ShuttleRouteName.values()) {
            if (region.getBusType().equals(type)) {
                routes.add(region);
            }
        }
        return routes;
    }
}
