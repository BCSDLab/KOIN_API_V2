package in.koreatech.koin.admin.bus.shuttle.dto.responose;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminShuttleBueTimeTableResponse(
    @Schema(description = "문서 ID")
    String id,

    @Schema(description = "정류소 정보 리스트")
    List<NodeInfo> nodeInfo,

    @Schema(description = "운행 지역", example = "CHEONAN_ASAN")
    String region,

    @Schema(description = "회차별 도착 시간 및 운행 요일 정보 리스트")
    List<RouteInfo> routeInfo,

    @Schema(description = "노선 이름", example = "천안 셔틀")
    String routeName,

    @Schema(description = "노선 타입", example = "SHUTTLE")
    String routeType,

    @Schema(description = "학기 구분", example = "정규학기")
    String semesterType,

    @Schema(description = "노선 부제목", example = "수신반점")
    String subName
) {

    public record NodeInfo(

        @Schema(description = "정류소 이름", example = "한기대")
        String name,

        @Schema(description = "정류소 이름 추가 설명 (없으면 null)", example = "학화호두과자 앞")
        String detail
    ) {}

    public record RouteInfo(

        @Schema(description = "회차 이름", example = "1회")
        String name,

        @Schema(description = "운행 요일 리스트")
        List<String> runningDays,

        @Schema(description = "각 정류소 별 도착 시간 (미정차인 경우 null)")
        List<String> arrivalTime
    ) {}
}
