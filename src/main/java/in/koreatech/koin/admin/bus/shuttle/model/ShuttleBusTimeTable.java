package in.koreatech.koin.admin.bus.shuttle.model;

import java.util.List;

import in.koreatech.koin.admin.bus.shuttle.enums.RunningDays;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class ShuttleBusTimeTable {

    private String id;
    private String semesterType;
    private String region;
    private String routeType;
    private String routeName;
    private String subName;
    private List<NodeInfo> nodeInfos;
    private List<RouteInfo> routeInfos;

    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    public static class NodeInfo {

        private String name;
        private String detail;

        public static NodeInfo of(String name, String detail) {
            return new NodeInfo(name, detail);
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
}
