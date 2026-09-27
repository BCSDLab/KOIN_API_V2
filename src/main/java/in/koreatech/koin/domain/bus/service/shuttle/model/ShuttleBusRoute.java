package in.koreatech.koin.domain.bus.service.shuttle.model;

import static lombok.AccessLevel.PROTECTED;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
        validateRouteShape(nodeInfos, routeInfos);

        Map<String, List<Integer>> existingRouteIndexesByName = new HashMap<>();
        for (int index = 0; index < this.routeInfo.size(); index++) {
            existingRouteIndexesByName.computeIfAbsent(this.routeInfo.get(index).getName(), ignored -> new ArrayList<>())
                .add(index);
        }

        Map<String, Integer> consumedRouteCounts = new HashMap<>();
        Set<Integer> matchedRouteIndexes = new HashSet<>();
        for (RouteInfo updatedRouteInfo : routeInfos) {
            List<Integer> existingRouteIndexes = existingRouteIndexesByName.get(updatedRouteInfo.getName());
            int occurrence = consumedRouteCounts.getOrDefault(updatedRouteInfo.getName(), 0);
            if (existingRouteIndexes == null || occurrence >= existingRouteIndexes.size()) {
                throw invalidRequest("부분 수정에서는 기존 회차 이름만 변경할 수 있습니다.");
            }

            matchedRouteIndexes.add(existingRouteIndexes.get(occurrence));
            consumedRouteCounts.put(updatedRouteInfo.getName(), occurrence + 1);
        }

        validateDuplicateRouteCounts(existingRouteIndexesByName, consumedRouteCounts);
        boolean omittedRoute = matchedRouteIndexes.size() != this.routeInfo.size();
        if (omittedRoute && !hasSameNodeIdentityAndOrder(this.nodeInfo, nodeInfos)) {
            throw invalidRequest("생략된 회차가 있는 부분 수정에서는 정류장 이름과 순서를 변경할 수 없습니다.");
        }

        this.nodeInfo = copyNodeInfos(nodeInfos);
        consumedRouteCounts.clear();
        for (RouteInfo updatedRouteInfo : routeInfos) {
            List<Integer> existingRouteIndexes = existingRouteIndexesByName.get(updatedRouteInfo.getName());
            int occurrence = consumedRouteCounts.getOrDefault(updatedRouteInfo.getName(), 0);
            RouteInfo routeInfo = this.routeInfo.get(existingRouteIndexes.get(occurrence));
            if (updatedRouteInfo.getDetail() != null) {
                routeInfo.detail = updatedRouteInfo.getDetail();
            }
            routeInfo.arrivalTime = copyList(updatedRouteInfo.getArrivalTime());
            if (!CollectionUtils.isEmpty(updatedRouteInfo.getRunningDays())) {
                routeInfo.runningDays = copyList(updatedRouteInfo.getRunningDays());
            }
            consumedRouteCounts.put(updatedRouteInfo.getName(), occurrence + 1);
        }
    }

    public void replaceRoute(List<NodeInfo> nodeInfos, List<RouteInfo> routeInfos) {
        validateRouteShape(nodeInfos, routeInfos);
        this.nodeInfo = copyNodeInfos(nodeInfos);
        this.routeInfo = copyRouteInfos(routeInfos);
    }

    public ShuttleBusRoute copy() {
        return ShuttleBusRoute.builder()
            .id(id)
            .semesterType(semesterType)
            .region(region)
            .routeType(routeType)
            .routeName(routeName)
            .subName(subName)
            .nodeInfo(copyNodeInfos(nodeInfo))
            .routeInfo(copyRouteInfos(routeInfo))
            .build();
    }

    public static void validateRouteShape(List<NodeInfo> nodeInfos, List<RouteInfo> routeInfos) {
        if (CollectionUtils.isEmpty(nodeInfos) || CollectionUtils.isEmpty(routeInfos)) {
            throw invalidRequest("정류장과 회차 정보는 비어 있을 수 없습니다.");
        }
        if (nodeInfos.stream().anyMatch(nodeInfo -> nodeInfo == null || isBlank(nodeInfo.getName()))) {
            throw invalidRequest("정류장 이름은 비어 있을 수 없습니다.");
        }
        for (RouteInfo routeInfo : routeInfos) {
            if (routeInfo == null || isBlank(routeInfo.getName())) {
                throw invalidRequest("회차 이름은 비어 있을 수 없습니다.");
            }
            if (routeInfo.getArrivalTime() == null || routeInfo.getArrivalTime().size() != nodeInfos.size()) {
                throw invalidRequest("회차별 도착 시간 개수는 정류장 개수와 같아야 합니다.");
            }
        }
    }

    private void validateDuplicateRouteCounts(
        Map<String, List<Integer>> existingRouteIndexesByName,
        Map<String, Integer> updatedRouteCountsByName
    ) {
        for (Map.Entry<String, Integer> entry : updatedRouteCountsByName.entrySet()) {
            int existingRouteCount = existingRouteIndexesByName.get(entry.getKey()).size();
            int updatedRouteCount = entry.getValue();
            if ((existingRouteCount > 1 || updatedRouteCount > 1)
                && existingRouteCount != updatedRouteCount) {
                throw CustomException.of(
                    INVALID_REQUEST_BODY,
                    "동일한 회차 이름의 기존 회차 수와 요청 회차 수가 다릅니다: " + entry.getKey()
                );
            }
        }
    }

    private boolean hasSameNodeIdentityAndOrder(List<NodeInfo> currentNodes, List<NodeInfo> updatedNodes) {
        if (currentNodes == null || currentNodes.size() != updatedNodes.size()) {
            return false;
        }
        for (int index = 0; index < currentNodes.size(); index++) {
            if (!Objects.equals(currentNodes.get(index).getName(), updatedNodes.get(index).getName())) {
                return false;
            }
        }
        return true;
    }

    private static List<NodeInfo> copyNodeInfos(List<NodeInfo> source) {
        return copyList(source, nodeInfo -> NodeInfo.builder()
            .name(nodeInfo.getName())
            .detail(nodeInfo.getDetail())
            .build());
    }

    private static List<RouteInfo> copyRouteInfos(List<RouteInfo> source) {
        return copyList(source, routeInfo -> RouteInfo.builder()
            .name(routeInfo.getName())
            .detail(routeInfo.getDetail())
            .runningDays(copyList(routeInfo.getRunningDays()))
            .arrivalTime(copyList(routeInfo.getArrivalTime()))
            .build());
    }

    private static <T> List<T> copyList(List<T> source) {
        return source == null ? null : new ArrayList<>(source);
    }

    private static <T, R> List<R> copyList(List<T> source, java.util.function.Function<T, R> mapper) {
        return source == null ? null : source.stream().map(mapper).toList();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static CustomException invalidRequest(String detail) {
        return CustomException.of(INVALID_REQUEST_BODY, detail);
    }
}
