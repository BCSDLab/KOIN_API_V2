package in.koreatech.koin.domain.bus.service.shuttle.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.language.service.OfficialNameResolver;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
@Schema(description = "셔틀버스 노선 응답")
public record ShuttleBusTimetableResponse(
    @Schema(description = "노선 ID", example = "675013f9465776d6265ddfdb")
    String id,

    @Schema(description = "지역 이름", example = "천안")
    String region,

    @Schema(description = "노선 타입", example = "순환")
    String routeType,

    @Schema(description = "노선 이름", example = "천안 셔틀")
    String routeName,

    @Schema(description = "노선 부가 이름", example = "null")
    String subName,

    @Schema(description = "정류장 정보 목록")
    List<NodeInfoResponse> nodeInfo,

    @Schema(description = "회차 정보 목록")
    List<RouteInfoResponse> routeInfo
) {

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(description = "정류장 정보")
    public record NodeInfoResponse(
        @Schema(description = "정류장 이름", example = "캠퍼스 정문")
        String name,

        @Schema(description = "정류장 세부 정보", example = "정문 앞 정류장")
        String detail,

        @Schema(description = "표시용 공식 정류장명. 해당 언어의 공식 명칭이 없으면 name 원문", example = "Cheonan Station")
        String displayName
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(description = "노선 정보")
    public record RouteInfoResponse(
        @Schema(description = "노선 이름", example = "1회")
        String name,

        @Schema(description = "노선 세부 정보", example = "등교")
        String detail,

        @Schema(description = "도착 시간 목록", example = "[\"08:00\", \"09:00\"]")
        List<String> arrivalTime
    ) {
    }

    public static ShuttleBusTimetableResponse from(ShuttleBusRoute shuttleBusRoute) {
        List<NodeInfoResponse> nodeInfoResponses = shuttleBusRoute.getNodeInfo().stream()
            .map(node -> new NodeInfoResponse(node.getName(), node.getDetail(), node.getName()))
            .toList();
        List<RouteInfoResponse> routeInfoResponses = shuttleBusRoute.getRouteInfo().stream()
            .map(route -> new RouteInfoResponse(route.getName(), route.getDetail(), route.getArrivalTime()))
            .toList();
        return new ShuttleBusTimetableResponse(
            shuttleBusRoute.getId(),
            shuttleBusRoute.getRegion().getLabel(),
            shuttleBusRoute.getRouteType().getLabel(),
            shuttleBusRoute.getRouteName(),
            shuttleBusRoute.getSubName(),
            nodeInfoResponses,
            routeInfoResponses
        );
    }

    public ShuttleBusTimetableResponse withLanguage(SupportedLanguage language) {
        return new ShuttleBusTimetableResponse(id, region, routeType, routeName, subName,
            nodeInfo.stream().map(node -> new NodeInfoResponse(node.name(), node.detail(),
                OfficialNameResolver.stationName(node.name(), language))).toList(), routeInfo);
    }
}
