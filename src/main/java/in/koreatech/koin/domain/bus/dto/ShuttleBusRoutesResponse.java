package in.koreatech.koin.domain.bus.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.model.mongo.ShuttleBusRoute;
import in.koreatech.koin.domain.version.dto.VersionMessageResponse;

@JsonNaming(SnakeCaseStrategy.class)
public record ShuttleBusRoutesResponse(
    List<RouteRegion> shuttleRoutes,
    List<RouteRegion> commutingRoutes,
    List<RouteRegion> weekendRoutes,
    RouteSemester semesterInfo
) {

    @JsonNaming(SnakeCaseStrategy.class)
    private record RouteRegion(String region, List<RouteName> routes) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record RouteName(String id, String routeName, String subName) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record RouteSemester(String name, String term) {
    }

    public static ShuttleBusRoutesResponse from(List<ShuttleBusRoute> shuttleBusRoutes,
        VersionMessageResponse versionMessageResponse) {
        Map<String, List<ShuttleBusRoute>> groupedByRouteType = shuttleBusRoutes.stream()
            .collect(Collectors.groupingBy(ShuttleBusRoute::getRouteType));
        List<RouteRegion> shuttleRoutes = mapRegions(groupedByRouteType.getOrDefault("순환", new ArrayList<>()));
        List<RouteRegion> commutingRoutes = mapRegions(groupedByRouteType.getOrDefault("주중", new ArrayList<>()));
        List<RouteRegion> weekendRoutes = mapRegions(groupedByRouteType.getOrDefault("주말", new ArrayList<>()));
        RouteSemester routeSemester = new RouteSemester(versionMessageResponse.title(), versionMessageResponse.content());
        return new ShuttleBusRoutesResponse(shuttleRoutes, commutingRoutes, weekendRoutes, routeSemester);
    }

    private static List<RouteRegion> mapRegions(List<ShuttleBusRoute> routes) {
        return routes.stream()
            .collect(Collectors.groupingBy(ShuttleBusRoute::getRegion))
            .entrySet().stream()
            .map(entry -> new RouteRegion(entry.getKey(), mapRouteNames(entry.getValue())))
            .toList();
    }

    private static List<RouteName> mapRouteNames(List<ShuttleBusRoute> routes) {
        return routes.stream()
            .map(route -> new RouteName(route.getId(), route.getRouteName(), route.getSubName()))
            .toList();
    }
}
