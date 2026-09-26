package in.koreatech.koin.domain.bus.service.shuttle.model;

import static lombok.AccessLevel.PROTECTED;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.util.CollectionUtils;

import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.global.exception.CustomException;
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

    public void updateCommutingBusRoute(
        List<NodeInfo> nodeInfos,
        List<RouteInfo> routeInfos
    ) {
        Map<String, List<RouteInfo>> updatedRoutesByName = routeInfos.stream()
            .collect(Collectors.groupingBy(RouteInfo::getName));
        validateDuplicateRouteCounts(updatedRoutesByName);

        this.nodeInfo = nodeInfos;
        Map<String, Integer> consumedRouteCounts = new HashMap<>();
        for (RouteInfo routeInfo : this.routeInfo) {
            List<RouteInfo> updatedRoutes = updatedRoutesByName.get(routeInfo.getName());
            if (updatedRoutes == null) {
                continue;
            }

            int occurrence = consumedRouteCounts.getOrDefault(routeInfo.getName(), 0);
            RouteInfo updatedRouteInfo = updatedRoutes.get(occurrence);
            routeInfo.arrivalTime = updatedRouteInfo.getArrivalTime();
            if (!CollectionUtils.isEmpty(updatedRouteInfo.getRunningDays())) {
                routeInfo.runningDays = updatedRouteInfo.getRunningDays();
            }
            consumedRouteCounts.put(routeInfo.getName(), occurrence + 1);
        }
    }

    private void validateDuplicateRouteCounts(Map<String, List<RouteInfo>> updatedRoutesByName) {
        for (Map.Entry<String, List<RouteInfo>> entry : updatedRoutesByName.entrySet()) {
            long existingRouteCount = this.routeInfo.stream()
                .filter(routeInfo -> Objects.equals(routeInfo.getName(), entry.getKey()))
                .count();
            long updatedRouteCount = entry.getValue().size();

            if ((existingRouteCount > 1 || updatedRouteCount > 1)
                && existingRouteCount != updatedRouteCount) {
                throw CustomException.of(
                    INVALID_REQUEST_BODY,
                    "동일한 회차 이름의 기존 회차 수와 요청 회차 수가 다릅니다: " + entry.getKey()
                );
            }
        }
    }
}
