package in.koreatech.koin.admin.bus.commuting.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.bus.commuting.model.NodeInfo;
import in.koreatech.koin.admin.bus.commuting.model.RouteInfo;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminCommutingBusResponse(
    String region,
    String routeType,
    String routeName,
    String subName,
    List<InnerNodeInfoResponse> nodeInfo,
    List<InnerRouteInfoResponse> routeInfo
) {
    public record InnerNodeInfoResponse(
        String name,
        String detail
    ) {
        public static InnerNodeInfoResponse of(String name, String detail) {
            return new InnerNodeInfoResponse(name, detail);
        }
    }

    public record InnerRouteInfoResponse(
        String name,
        String detail,
        List<String> arrivalTime
    ) {
        public static InnerRouteInfoResponse of(String name, String detail, List<String> arrivalTime) {
            return new InnerRouteInfoResponse(name, detail, arrivalTime);
        }
    }

    public static AdminCommutingBusResponse of(
        String region,
        String routeType,
        String routeName,
        String subName,
        List<NodeInfo> nodeInfos,
        List<RouteInfo> routeInfos
    ) {
        return new AdminCommutingBusResponse(
            region,
            routeType,
            routeName,
            subName,
            nodeInfos.stream()
                .map(nodeInfo -> InnerNodeInfoResponse.of(nodeInfo.name(), nodeInfo.detail()))
                .toList(),
            routeInfos.stream()
                .map(routeInfo -> InnerRouteInfoResponse.of(routeInfo.getName(), routeInfo.getDetail(),
                    routeInfo.getArrivalTimesAsStringList()))
                .toList()
        );
    }
}
