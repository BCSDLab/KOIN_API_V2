package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ApplicationCreatedResponse(
    @Schema(description = "지원서 ID", example = "51", requiredMode = REQUIRED)
    Integer applicationId,

    @Schema(description = "모집글 ID", example = "17", requiredMode = REQUIRED)
    Integer recruitmentId,

    @Schema(description = "지원 상태", example = "PENDING", requiredMode = REQUIRED)
    TeamRecruitmentApplicationStatus status,

    @Schema(description = "선택 역할, GENERAL 모집은 null", nullable = true, requiredMode = REQUIRED)
    ApplicationRole role,

    @Schema(description = "생성 일시(KST)", example = "2026-08-26 11:40:00", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt
) {
}
