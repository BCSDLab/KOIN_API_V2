package in.koreatech.koin.domain.callvan.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record CallvanPostCreateRequest(
    @Schema(description = "출발지 타입", example = "FRONT_GATE", requiredMode = REQUIRED)
    @NotNull(message = "출발물 타입은 필수입니다.")
    CallvanLocation departureType,

    @Schema(description = "출발지 직접 입력 (기타 선택 시)", example = "우리 집")
    String departureCustomName,

    @Schema(description = "도착지 타입", example = "TERMINAL", requiredMode = REQUIRED)
    @NotNull(message = "도착지 타입은 필수입니다.")
    CallvanLocation arrivalType,

    @Schema(description = "도착지 직접 입력 (기타 선택 시)", example = "한기대 제2캠퍼스")
    String arrivalCustomName,

    @Schema(description = "출발일", example = "2024-03-25", requiredMode = REQUIRED)
    @NotNull(message = "출발일은 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate departureDate,

    @Schema(description = "출발 시각", example = "15:30", requiredMode = REQUIRED)
    @NotNull(message = "출발 시각은 필수입니다.")
    @JsonFormat(pattern = "HH:mm")
    LocalTime departureTime,

    @Schema(description = "모집 인원 (2~11명)", example = "4", requiredMode = REQUIRED)
    @NotNull(message = "모집 인원은 필수입니다.")
    @Min(value = 2, message = "모집 인원은 최소 2명입니다.")
    @Max(value = 11, message = "모집 인원은 최대 11명입니다.")
    Integer maxParticipants
) {

}
