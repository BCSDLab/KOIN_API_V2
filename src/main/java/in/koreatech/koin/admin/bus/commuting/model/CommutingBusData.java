package in.koreatech.koin.admin.bus.commuting.model;

import java.util.List;

public record CommutingBusData(
    NodeInfos nodeInfos,
    List<RouteInfo> routeInfos
) {

}

