package in.koreatech.koin.domain.bus.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.model.mongo.ShuttleBusRoute;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
@Schema(description = "셔틀버스 경로 응답 DTO")
public record ShuttleBusRouteResponse(
    @Schema(description = "경로 ID", example = "675013f9465776d6265ddfdb")
    String id,

    @Schema(description = "지역 이름", example = "천안")
    String region,

    @Schema(description = "경로 타입", example = "순환")
    String routeType,

    @Schema(description = "경로 이름", example = "천안 셔틀")
    String routeName,

    @Schema(description = "경로 부가 이름", example = "null")
    String subName,

    @Schema(description = "노드 정보 목록")
    List<NodeInfoResponse> nodeInfo,

    @Schema(description = "경로 정보 목록")
    List<RouteInfoResponse> routeInfo
) {

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(description = "노드 정보")
    private record NodeInfoResponse(
        @Schema(description = "노드 이름", example = "캠퍼스 정문")
        String name,

        @Schema(description = "노드 세부 정보", example = "정문 앞 정류장")
        String detail
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(description = "경로 정보")
    private record RouteInfoResponse(
        @Schema(description = "경로 이름", example = "1회")
        String name,

        @Schema(description = "도착 시간 목록", example = "[\"08:00\", \"09:00\"]")
        List<String> arrivalTime
    ) {
    }

    public static ShuttleBusRouteResponse from(ShuttleBusRoute shuttleBusRoute) {
        List<NodeInfoResponse> nodeInfoResponses = shuttleBusRoute.getNodeInfo().stream()
            .map(node -> new NodeInfoResponse(node.getName(), node.getDetail()))
            .toList();
        List<RouteInfoResponse> routeInfoResponses = shuttleBusRoute.getRouteInfo().stream()
            .map(route -> new RouteInfoResponse(route.getName(), route.getArrivalTime()))
            .toList();
        return new ShuttleBusRouteResponse(
            shuttleBusRoute.getId(),
            shuttleBusRoute.getRegion(),
            shuttleBusRoute.getRouteType(),
            shuttleBusRoute.getRouteName(),
            shuttleBusRoute.getSubName(),
            nodeInfoResponses,
            routeInfoResponses
        );
    }
}
