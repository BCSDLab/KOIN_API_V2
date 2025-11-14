package in.koreatech.koin.admin.bus.commuting.model;

public record RouteInfos(
    RouteInfo northRouteInfo,
    RouteInfo southRouteInfo
) {
    public static RouteInfos of(RouteInfo northRouteInfo, RouteInfo southRouteInfo) {
        return new RouteInfos(northRouteInfo, southRouteInfo);
    }
}
