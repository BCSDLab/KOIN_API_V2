package in.koreatech.koin.admin.bus.shuttle.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimetable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminShuttleBusTimetableResponse(
    @Schema(description = "셔틀 버스 시간표 정보 리스트")
    List<InnerAdminShuttleBusTimetableResponse> shuttleBusTimetables
) {
    @JsonNaming(SnakeCaseStrategy.class)
    @Builder
    public record InnerAdminShuttleBusTimetableResponse(
        @Schema(description = "운행 지역", example = "CHEONAN_ASAN", requiredMode = REQUIRED)
        String region,

        @Schema(description = "노선 타입", example = "SHUTTLE", requiredMode = REQUIRED)
        String routeType,

        @Schema(description = "노선 이름", example = "천안 셔틀", requiredMode = REQUIRED)
        String routeName,

        @Schema(description = "노선 부제목", example = "토요일, 일요일", requiredMode = NOT_REQUIRED)
        String subName,

        @Schema(description = "정류소 정보 리스트")
        List<InnerNodeInfoResponse> nodeInfo,

        @Schema(description = "회차별 도착 시간 및 운행 요일 정보 리스트")
        List<InnerRouteInfoResponse> routeInfo
    ) {
        public static InnerAdminShuttleBusTimetableResponse from(ShuttleBusTimetable table) {
            List<InnerNodeInfoResponse> nodeInfo = table.getNodeInfos()
                .stream()
                .map(n -> new InnerNodeInfoResponse(
                    n.getName(),
                    n.getDetail()
                ))
                .toList();

            List<InnerRouteInfoResponse> routeInfo = table.getRouteInfos()
                .stream()
                .map(r -> new InnerRouteInfoResponse(
                    r.getName(),
                    r.getDetail(),
                    r.getArrivalTime()
                ))
                .toList();

            return InnerAdminShuttleBusTimetableResponse.builder()
                .nodeInfo(nodeInfo)
                .region(table.getRegion())
                .routeInfo(routeInfo)
                .routeName(table.getRouteName())
                .routeType(table.getRouteType())
                .subName(table.getSubName())
                .build();
        }

        @JsonNaming(SnakeCaseStrategy.class)
        public record InnerNodeInfoResponse(
            @Schema(description = "정류소 이름", example = "한기대", requiredMode = REQUIRED)
            String name,

            @Schema(description = "정류소 이름 추가 설명 (없으면 null)", example = "학화호두과자 앞", requiredMode = NOT_REQUIRED)
            String detail
        ) {
        }

        @JsonNaming(SnakeCaseStrategy.class)
        public record InnerRouteInfoResponse(
            @Schema(description = "회차 이름", example = "1회", requiredMode = REQUIRED)
            String name,

            @Schema(description = "회차 세부 이름", example = "(청주역→본교)", requiredMode = NOT_REQUIRED)
            String detail,

            @Schema(
                description = "각 정류소 별 도착 시간 (미정차인 경우 null)",
                example = "[\"08:00\", \"09:00\"]",
                requiredMode = REQUIRED
            )
            List<String> arrivalTime
        ) {
        }
    }
}
