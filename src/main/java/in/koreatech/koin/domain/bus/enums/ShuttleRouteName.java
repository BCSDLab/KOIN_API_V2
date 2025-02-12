package in.koreatech.koin.domain.bus.enums;

import static in.koreatech.koin.domain.bus.enums.BusType.COMMUTING;
import static in.koreatech.koin.domain.bus.enums.BusType.SHUTTLE;
import static in.koreatech.koin.domain.bus.enums.ShuttleBusRegion.*;
import static in.koreatech.koin.domain.bus.enums.ShuttleRouteType.WEEKDAYS;
import static in.koreatech.koin.domain.bus.enums.ShuttleRouteType.WEEKEND;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.koreatech.koin.domain.bus.exception.BusIllegalRegionException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShuttleRouteName {
    COMMUTING_CHEONAN("천안", COMMUTING, CHEONAN_ASAN, List.of(WEEKDAYS)),
    COMMUTING_CHEONGJU("청주", COMMUTING, CHEONGJU, List.of(WEEKDAYS)),
    COMMUTING_SEOUL("서울", COMMUTING, SEOUL, List.of(WEEKDAYS)),
    COMMUTING_DAEJEON("대전", COMMUTING, DAEJEON_SEJONG, List.of(WEEKDAYS)),
    COMMUTING_SEJONG("세종", COMMUTING, DAEJEON_SEJONG, List.of(WEEKDAYS)),

    SHUTTLE_CHEONAN("천안", SHUTTLE, CHEONAN_ASAN, List.of(ShuttleRouteType.SHUTTLE, WEEKEND)),
    SHUTTLE_CHEONGJU("청주", SHUTTLE, CHEONGJU, List.of(ShuttleRouteType.SHUTTLE));

    private final String regionName;
    private final BusType busType;
    private final ShuttleBusRegion newRegionName;
    private final List<ShuttleRouteType> newBusType;

    public static ShuttleRouteName getRegionByLegacy(BusType type, String regionName) {
        for (ShuttleRouteName region : values()) {
            if (Objects.equals(region.getBusType(), type)
                && Objects.equals(region.getRegionName(), regionName)) {
                return region;
            }
        }
        throw BusIllegalRegionException.withDetail("displayName: " + regionName);
    }

    public static List<ShuttleRouteName> getRoutesByLegacy(BusType type) {
        List<ShuttleRouteName> routes = new ArrayList<>();
        for (ShuttleRouteName region : values()) {
            if (region.getBusType().equals(type)) {
                routes.add(region);
            }
        }
        return routes;
    }
}
