package in.koreatech.koin.admin.bus.shuttle.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.*;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimeTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@JsonNaming(SnakeCaseStrategy.class)
@Builder
public record AdminShuttleBusTimeTableResponse(
    @Schema(description = "운행 지역", example = "CHEONAN_ASAN", requiredMode = REQUIRED)
    String region,

    @Schema(description = "노선 타입", example = "SHUTTLE", requiredMode = REQUIRED)
    String routeType,

    @Schema(description = "노선 이름", example = "천안 셔틀", requiredMode = REQUIRED)
    String routeName,

    @Schema(description = "노선 부제목", example = "토요일, 일요일", requiredMode = NOT_REQUIRED)
    String subName,

    @Schema(description = "정류소 정보 리스트")
    List<NodeInfo> nodeInfo,

    @Schema(description = "회차별 도착 시간 및 운행 요일 정보 리스트")
    List<RouteInfo> routeInfo
) {

    public static AdminShuttleBusTimeTableResponse from(ShuttleBusTimeTable table) {
        List<NodeInfo> nodeInfos = table.getNodeInfos().stream()
            .map(n -> new AdminShuttleBusTimeTableResponse.NodeInfo(
                n.getName(),
                n.getDetail()
            ))
            .toList();

        List<RouteInfo> routeInfos = table.getRouteInfos().stream()
            .map(r -> new AdminShuttleBusTimeTableResponse.RouteInfo(
                r.getName(),
                r.getArrivalTime()
            ))
            .toList();

        return AdminShuttleBusTimeTableResponse.builder()
            .nodeInfo(nodeInfos)
            .region(table.getRegion().getValue())
            .routeInfo(routeInfos)
            .routeName(table.getRouteName().getName())
            .routeType(table.getRouteType().getValue())
            .subName(table.getRouteName().getSubName())
            .build();
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record NodeInfo(
        @Schema(description = "정류소 이름", example = "한기대", requiredMode = REQUIRED)
        String name,

        @Schema(description = "정류소 이름 추가 설명 (없으면 null)", example = "학화호두과자 앞", requiredMode = NOT_REQUIRED)
        String detail
    ) {}

    @JsonNaming(SnakeCaseStrategy.class)
    public record RouteInfo(
        @Schema(description = "회차 이름", example = "1회", requiredMode = REQUIRED)
        String name,

        @Schema(description = "각 정류소 별 도착 시간 (미정차인 경우 null)", requiredMode = REQUIRED)
        List<String> arrivalTime
    ) {}
}
