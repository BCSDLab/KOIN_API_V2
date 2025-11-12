package in.koreatech.koin.admin.bus.shuttle.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminShuttleBusUpdateRequest(
    @Schema(description = "버스 시간표 정보 리스트", requiredMode = REQUIRED)
    @NotEmpty(message = "버스 시간표 정보 리스트가 비어있습니다.")
    List<InnerAdminShuttleBusUpdateRequest> shuttleBusTimeTables
) {

    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerAdminShuttleBusUpdateRequest(
        @Schema(description = "지역 이름", example = "천안・아산", requiredMode = REQUIRED)
        @NotBlank(message = "지역 이름이 비어있습니다.")
        String region,

        @Schema(description = "노선 형태", example = "순환", requiredMode = REQUIRED)
        @NotBlank(message = "노선 형태가 비어있습니다.")
        String routeType,

        @Schema(description = "노선 이름", example = "천안 셔틀", requiredMode = REQUIRED)
        @NotBlank(message = "노선 이름이 비어있습니다.")
        String routeName,

        @Schema(description = "노선 세부 이름", example = "토요일, 일요일", requiredMode = NOT_REQUIRED)
        String subName,

        @Schema(description = "정류소 정보 리스트", requiredMode = REQUIRED)
        @NotEmpty(message = "정류소 정보 리스트가 비어있습니다.")
        List<InnerNodeInfoRequest> nodeInfo,

        @Schema(description = "회차 정보 리스트", requiredMode = REQUIRED)
        @NotEmpty(message = "회차 정보 리스트가 비어있습니다.")
        List<InnerRouteInfoRequest> routeInfo
    ) {

        @JsonNaming(SnakeCaseStrategy.class)
        public record InnerNodeInfoRequest(
            @Schema(description = "정류소 이름", example = "천안역", requiredMode = REQUIRED)
            @NotBlank(message = "정류소 이름이 비어있습니다.")
            String name,

            @Schema(description = "정류소 세부 이름", example = "학화호두과자 앞", requiredMode = NOT_REQUIRED)
            String detail
        ) {

        }

        @JsonNaming(SnakeCaseStrategy.class)
        public record InnerRouteInfoRequest(
            @Schema(description = "회차 이름", example = "1회", requiredMode = REQUIRED)
            @NotBlank(message = "회차 이름이 비어있습니다.")
            String name,

            @Schema(description = "회차 세부 이름", example = "(청주역→본교)", requiredMode = NOT_REQUIRED)
            String detail,

            @Schema(description = "각 정류소 별 도착 시간", example = "[\"11:10\", null, \"11:35\"]", requiredMode = REQUIRED)
            @NotEmpty(message = "도착 시간 리스트가 비어있습니다.")
            List<String> arrivalTime
        ) {

        }
    }


}
