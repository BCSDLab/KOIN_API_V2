package in.koreatech.koin.domain.bus.service.shuttle.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.domain.version.dto.VersionMessageResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
@Schema(description = "셔틀버스 경로 응답")
public record ShuttleBusRoutesResponse(
    @Schema(description = "노선 지역 분류 목록") List<RouteRegion> routeRegions,
    @Schema(description = "학기 정보") RouteSemester semesterInfo
) {

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(description = "노선 지역 정보")
    public record RouteRegion(
        @Schema(description = "지역 이름", example = "천안")
        String region,

        @Schema(description = "해당 지역의 경로 목록")
        List<RouteName> routes
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(description = "노선 세부 정보")
    public record RouteName(
        @Schema(description = "노선 ID", example = "675013f9465776d6265ddfdb") String id,
        @Schema(description = "노선 종류", example = "주말") String type,
        @Schema(description = "노선 이름", example = "대학원") String routeName,
        @Schema(description = "노선 부가 이름", example = "토요일") String subName
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(description = "학기 정보")
    public record RouteSemester(
        @Schema(description = "학기 이름", example = "정규학기") String name,
        @Schema(description = "학기 시작 날짜", example = "2024-09-02") String from,
        @Schema(description = "학기 종료 날짜", example = "2024-12-20") String to
    ) {
    }

    public static ShuttleBusRoutesResponse of(List<ShuttleBusRoute> shuttleBusRoutes,
        VersionMessageResponse versionMessageResponse) {
        List<RouteRegion> categories = mapCategories(shuttleBusRoutes);
        String[] term = versionMessageResponse.content().split("~");
        RouteSemester routeSemester = new RouteSemester(versionMessageResponse.title(), term[0].trim(), term[1].trim());
        return new ShuttleBusRoutesResponse(categories, routeSemester);
    }

    private static List<RouteRegion> mapCategories(List<ShuttleBusRoute> shuttleBusRoutes) {
        return shuttleBusRoutes.stream()
            .collect(Collectors.groupingBy(ShuttleBusRoute::getRegion))
            .entrySet().stream()
            .map(entry -> new RouteRegion(entry.getKey().getLabel(), mapRouteNames(entry.getValue())))
            .sorted(Comparator.comparingInt(o -> ShuttleBusRegion.getOrdinalByLabel(o.region())))
            .toList();
    }

    private static List<RouteName> mapRouteNames(List<ShuttleBusRoute> routes) {
        return routes.stream()
            .map(route -> new RouteName(route.getId(), route.getRouteType().getLabel(), route.getRouteName(),
                route.getSubName()))
            .sorted(Comparator.comparingInt(o -> ShuttleRouteType.getOrdinalByLabel(o.type())))
            .toList();
    }
}
