package in.koreatech.koin.admin.bus.commuting.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminCommutingBusUpdateRequest(
    @Schema(description = "지역 이름", example = "천안", requiredMode = REQUIRED)
    @NotBlank(message = "지역 이름은 필수 입력값입니다.")
    String region,

    @Schema(description = "노선 타입", example = "순환", requiredMode = REQUIRED)
    @NotBlank(message = "노선 타입은 필수 입력값입니다.")
    String routeType,

    @Schema(description = "노선 이름", example = "천안 셔틀", requiredMode = REQUIRED)
    @NotBlank(message = "노선 이름은 필수 입력값입니다.")
    String routeName,

    @Schema(description = "노선 부가 이름", example = "null", requiredMode = NOT_REQUIRED)
    String subName,

    @Schema(description = "정류장 정보 목록")
    @NotEmpty(message = "정류장 정보 목록은 필수 입력값입니다.")
    List<InnerNodeInfoRequest> nodeInfo,

    @Schema(description = "회차 정보 목록")
    @NotEmpty(message = "회차 정보 목록은 필수 입력값입니다.")
    List<InnerRouteInfoUpdateRequest> routeInfo
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerNodeInfoRequest(
        @Schema(description = "정류장 이름", example = "캠퍼스 정문", requiredMode = REQUIRED)
        @NotBlank(message = "정류장 이름은 필수 입력값입니다.")
        String name,

        @Schema(description = "정류장 세부 정보", example = "정문 앞 정류장", requiredMode = NOT_REQUIRED)
        String detail
    ) {

    }

    public record InnerRouteInfoUpdateRequest(
        @Schema(description = "노선 이름", example = "등교", requiredMode = REQUIRED)
        @NotBlank(message = "노선 이름은 필수 입력값입니다.")
        String name,

        @Schema(description = "노선 세부 정보", example = "null", requiredMode = NOT_REQUIRED)
        String detail,

        @Schema(description = "도착 시간 목록", example = "[\"08:00\", \"09:00\"]", requiredMode = REQUIRED)
        @NotEmpty(message = "도착 시간 목록은 필수 입력값입니다.")
        List<String> arrivalTime
    ) {

    }
}
