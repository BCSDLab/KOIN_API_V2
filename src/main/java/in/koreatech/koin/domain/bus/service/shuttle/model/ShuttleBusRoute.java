package in.koreatech.koin.domain.bus.service.shuttle.model;

import static lombok.AccessLevel.PROTECTED;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Document(collection = "shuttlebus_timetables")
public class ShuttleBusRoute {

    @Id
    private String id;

    @Field("semester_type")
    private String semesterType;

    @Field("region")
    private ShuttleBusRegion region;

    @Field("route_type")
    private ShuttleRouteType routeType;

    @Field("route_name")
    private String routeName;

    @Field("sub_name")
    private String subName;

    @Field("node_info")
    private List<NodeInfo> nodeInfo;

    @Field("route_info")
    private List<RouteInfo> routeInfo;

    @Getter
    @NoArgsConstructor(access = PROTECTED)
    public static class NodeInfo {

        @Field("name")
        private String name;

        @Field("detail")
        private String detail;

        @Builder
        private NodeInfo(String name, String detail) {
            this.name = name;
            this.detail = detail;
        }
    }

    @Getter
    @NoArgsConstructor(access = PROTECTED)
    public static class RouteInfo {

        @Field("name")
        private String name;

        @Field("detail")
        private String detail;

        @Field("running_days")
        private List<String> runningDays;

        @Field("arrival_time")
        private List<String> arrivalTime;

        @Builder
        private RouteInfo(String name, String detail, List<String> runningDays, List<String> arrivalTime) {
            this.name = name;
            this.detail = detail;
            this.runningDays = runningDays;
            this.arrivalTime = arrivalTime;
        }
    }

    @Builder
    private ShuttleBusRoute(
        String id,
        String semesterType,
        ShuttleBusRegion region,
        ShuttleRouteType routeType,
        String routeName,
        String subName,
        List<NodeInfo> nodeInfo,
        List<RouteInfo> routeInfo
    ) {
        this.id = id;
        this.semesterType = semesterType;
        this.region = region;
        this.routeType = routeType;
        this.routeName = routeName;
        this.subName = subName;
        this.nodeInfo = nodeInfo;
        this.routeInfo = routeInfo;
    }
}
