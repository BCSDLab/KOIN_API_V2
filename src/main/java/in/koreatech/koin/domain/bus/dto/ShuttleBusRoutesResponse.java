package in.koreatech.koin.domain.bus.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.model.mongo.ShuttleBusRoute;
import in.koreatech.koin.domain.version.dto.VersionMessageResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
@Schema(description = "셔틀버스 경로 응답")
public record ShuttleBusRoutesResponse(
    @Schema(description = "순환 노선 경로 목록")
    List<RouteRegion> shuttleRoutes,
    @Schema(description = "주중 노선 경로 목록")
    List<RouteRegion> commutingRoutes,
    @Schema(description = "주말 노선 경로 목록")
    List<RouteRegion> weekendRoutes,
    @Schema(description = "학기 정보")
    RouteSemester semesterInfo
) {

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(description = "경로 지역 정보")
    private record RouteRegion(
        @Schema(description = "지역 이름", example = "천안") String region,
        @Schema(description = "해당 지역의 경로 목록") List<RouteName> routes
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(description = "경로 이름 정보")
    private record RouteName(
        @Schema(description = "경로 ID", example = "675013f9465776d6265ddfdb") String id,
        @Schema(description = "경로 이름", example = "대학원") String routeName,
        @Schema(description = "경로 부가 이름", example = "토요일") String subName
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(description = "학기 정보")
    private record RouteSemester(
        @Schema(description = "학기 이름", example = "정규학기") String name,
        @Schema(description = "학기 기간", example = "2024-09-02 ~ 2024-12-20") String term
    ) {
    }

    public static ShuttleBusRoutesResponse of(List<ShuttleBusRoute> shuttleBusRoutes,
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