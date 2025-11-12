package in.koreatech.koin.admin.bus.commuting.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.bus.commuting.model.NodeInfo;
import in.koreatech.koin.admin.bus.commuting.model.RouteInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminCommutingBusResponse(
    List<InnerAdminCommutingBusResponse> commutingBusTimetables
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerAdminCommutingBusResponse(
        @Schema(description = "지역 이름", example = "천안", requiredMode = REQUIRED)
        String region,

        @Schema(description = "노선 타입", example = "순환", requiredMode = REQUIRED)
        String routeType,

        @Schema(description = "노선 이름", example = "천안 셔틀", requiredMode = REQUIRED)
        String routeName,

        @Schema(description = "노선 부가 이름", example = "null", requiredMode = NOT_REQUIRED)
        String subName,

        @Schema(description = "정류장 정보 목록")
        List<InnerNodeInfoResponse> nodeInfo,

        @Schema(description = "회차 정보 목록")
        List<InnerRouteInfoResponse> routeInfo
    ) {
        @JsonNaming(value = SnakeCaseStrategy.class)
        public record InnerNodeInfoResponse(
            @Schema(description = "정류장 이름", example = "캠퍼스 정문", requiredMode = REQUIRED)
            String name,

            @Schema(description = "정류장 세부 정보", example = "정문 앞 정류장", requiredMode = NOT_REQUIRED)
            String detail
        ) {
            public static InnerNodeInfoResponse of(String name, String detail) {
                return new InnerNodeInfoResponse(name, detail);
            }
        }

        public record InnerRouteInfoResponse(
            @Schema(description = "노선 이름", example = "등교", requiredMode = REQUIRED)
            String name,

            @Schema(description = "노선 세부 정보", example = "null", requiredMode = NOT_REQUIRED)
            String detail,

            @Schema(description = "도착 시간 목록", example = "[\"08:00\", \"09:00\"]", requiredMode = REQUIRED)
            List<String> arrivalTime
        ) {
            public static InnerRouteInfoResponse of(String name, String detail, List<String> arrivalTime) {
                return new InnerRouteInfoResponse(name, detail, arrivalTime);
            }
        }

        public static InnerAdminCommutingBusResponse of(
            String region,
            String routeType,
            String routeName,
            String subName,
            List<NodeInfo> nodeInfos,
            List<RouteInfo> routeInfos
        ) {
            return new InnerAdminCommutingBusResponse(
                region,
                routeType,
                routeName,
                subName,
                nodeInfos.stream()
                    .map(nodeInfo -> InnerNodeInfoResponse.of(nodeInfo.name(), nodeInfo.detail()))
                    .toList(),
                routeInfos.stream()
                    .map(routeInfo -> InnerRouteInfoResponse.of(routeInfo.getName(), routeInfo.getDetail(),
                        routeInfo.getArrivalTimesAsStringList()))
                    .toList()
            );
        }
    }
}
