package in.koreatech.koin.admin.bus.commuting.model;

import in.koreatech.koin.admin.bus.commuting.enums.BusDirection;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;

public record CommutingBusExcelMetaData(
    BusDirection busDirection,
    ShuttleBusRegion busRegion,
    ShuttleRouteType routeType,
    String routeName,
    String routeSubName
) {
    public static CommutingBusExcelMetaData from(
        BusDirection busDirection,
        ShuttleBusRegion busRegion,
        ShuttleRouteType routeType,
        String routeName,
        String routeSubName
    ) {
        return new CommutingBusExcelMetaData(busDirection, busRegion, routeType, routeName, routeSubName);
    }
}
