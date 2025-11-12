package in.koreatech.koin.admin.bus.shuttle.model;

import static in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimeTable.RouteInfo.of;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest.InnerAdminShuttleBusUpdateRequest;
import in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest.InnerAdminShuttleBusUpdateRequest.InnerNodeInfoRequest;
import in.koreatech.koin.admin.bus.shuttle.enums.RunningDays;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Document(collection = "shuttlebus_timetables")
public class ShuttleBusTimeTable {

    @Id
    private String id;

    @Field(name = "semester_type")
    private String semesterType;

    @Field(name = "region")
    private String region;

    @Field(name = "route_type")
    private String routeType;

    @Field(name = "route_name")
    private String routeName;

    @Field(name = "sub_name")
    private String subName;

    @Field(name = "node_info")
    private List<NodeInfo> nodeInfos;

    @Field(name = "route_info")
    private List<RouteInfo> routeInfos;

    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    public static class NodeInfo {

        private String name;
        private String detail;

        public static NodeInfo of(String name, String detail) {
            return new NodeInfo(name, detail);
        }

        public static NodeInfo fromRequest(
            InnerNodeInfoRequest innerNodeInfo) {
            return new NodeInfo(innerNodeInfo.name(), innerNodeInfo.detail());
        }
    }

    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    @Getter
    public static class RouteInfo {

        private String name;
        private String detail;
        private List<String> runningDays;
        private List<String> arrivalTime;

        public static RouteInfo from(
            InnerNameDetail innerNameDetail, RunningDays runningDays, ArrivalTime arrivalTime) {
            return RouteInfo.builder()
                .name(innerNameDetail.name)
                .detail(innerNameDetail.detail)
                .runningDays(runningDays.getDays())
                .arrivalTime(arrivalTime.getTimes())
                .build();
        }

        public static RouteInfo of(String name, String detail, List<String> arrivalTime) {
            return RouteInfo.builder()
                .name(name)
                .detail(detail)
                .arrivalTime(arrivalTime)
                .build();
        }

        @AllArgsConstructor(access = AccessLevel.PROTECTED)
        @Getter
        public static class InnerNameDetail {

            private String name;
            private String detail;

            public static RouteInfo.InnerNameDetail of(String name, String detail) {
                return new RouteInfo.InnerNameDetail(name, detail);
            }
        }
    }

    public static ShuttleBusTimeTable from(
        List<NodeInfo> nodeInfos,
        List<RouteInfo> routeInfos,
        ShuttleBusRegion region,
        RouteName routeName,
        SubName subName,
        RouteType routeType
    ) {
        return ShuttleBusTimeTable.builder()
            .nodeInfos(nodeInfos)
            .routeInfos(routeInfos)
            .region(region.getLabel())
            .routeName(routeName.getName())
            .subName(subName.getName())
            .routeType(routeType.getValue())
            .build();
    }

    public void updateNodeInfo(List<NodeInfo> nodeInfos) {
        this.nodeInfos = nodeInfos;
    }

    public void updateRouteInfo(List<RouteInfo> routeInfos) {
        this.routeInfos = routeInfos;
    }

    public static ShuttleBusTimeTable fromRequest(
        InnerAdminShuttleBusUpdateRequest request,
        String semesterType
    ) {
        List<NodeInfo> nodeInfos = request.nodeInfo().stream()
            .map(node -> NodeInfo.of(node.name(), node.detail()))
            .toList();

        List<RouteInfo> routeInfos = request.routeInfo().stream()
            .map(route -> of(route.name(), route.detail(), route.arrivalTime()))
            .toList();

        return ShuttleBusTimeTable.builder()
            .semesterType(semesterType)
            .region(ShuttleBusRegion.convertFrom(request.region()).name())
            .routeType(ShuttleRouteType.convertFrom(request.routeType()).name())
            .routeName(request.routeName())
            .subName(request.subName())
            .nodeInfos(nodeInfos)
            .routeInfos(routeInfos)
            .build();
    }
}
