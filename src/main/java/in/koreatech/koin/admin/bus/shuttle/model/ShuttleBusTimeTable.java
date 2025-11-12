package in.koreatech.koin.admin.bus.shuttle.model;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class ShuttleBusTimeTable {

    private List<NodeInfo> nodeInfos;
    private List<RouteInfo> routeInfos;
    private Region region;
    private RouteName routeName;
    private RouteType routeType;

    public static ShuttleBusTimeTable from(
        List<NodeInfo> nodeInfos,
        List<RouteInfo> routeInfos,
        Region region,
        RouteName routeName,
        RouteType routeType
    ) {
        return ShuttleBusTimeTable.builder()
            .nodeInfos(nodeInfos)
            .routeInfos(routeInfos)
            .region(region)
            .routeName(routeName)
            .routeType(routeType)
            .build();
    }
}
