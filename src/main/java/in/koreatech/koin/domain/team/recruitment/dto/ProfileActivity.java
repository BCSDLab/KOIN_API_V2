package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ProfileActivity(
    @Schema(description = "활동 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "활동명", example = "AI 공모전", requiredMode = REQUIRED)
    String title,

    @Schema(description = "시작일", example = "2025-03-03", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startedAt,

    @Schema(description = "종료일", example = "2025-05-05", nullable = true, requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endedAt,

    @Schema(description = "진행 중 여부", example = "false", requiredMode = REQUIRED)
    Boolean isOngoing,

    @Schema(description = "활동 설명", example = "기획 담당", requiredMode = REQUIRED)
    String description
) {
}
