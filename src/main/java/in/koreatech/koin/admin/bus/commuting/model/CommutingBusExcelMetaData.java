package in.koreatech.koin.admin.bus.commuting.model;

import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_SHUTTLE_ROUTE_TYPE;

import in.koreatech.koin.admin.bus.commuting.enums.BusDirection;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.global.exception.CustomException;

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
        if (routeType.isNotCommuting()) {
            throw CustomException.of(INVALID_SHUTTLE_ROUTE_TYPE, "shuttleRouteType: " + routeType.name());
        }

        return new CommutingBusExcelMetaData(busDirection, busRegion, routeType, routeName, routeSubName);
    }
}
