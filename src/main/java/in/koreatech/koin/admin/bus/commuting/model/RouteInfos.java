package in.koreatech.koin.admin.bus.commuting.model;

public record RouteInfos(
    RouteInfo northRouteInfo,
    RouteInfo southRouteInfo
) {
    public static RouteInfos from(RouteInfo northRouteInfo, RouteInfo southRouteInfo) {
        return new RouteInfos(northRouteInfo, southRouteInfo);
    }
}
