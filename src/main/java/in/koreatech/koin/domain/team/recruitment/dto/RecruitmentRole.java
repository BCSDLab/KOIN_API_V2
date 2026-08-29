package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record RecruitmentRole(
    @Schema(description = "역할 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "역할명", example = "프론트엔드", requiredMode = REQUIRED)
    String name,

    @Schema(description = "승인된 지원자 수", example = "1", requiredMode = REQUIRED)
    Integer currentParticipants,

    @Schema(description = "역할 정원", example = "2", requiredMode = REQUIRED)
    Integer maxParticipants,

    @Schema(description = "역할 모집 마감 여부", example = "false", requiredMode = REQUIRED)
    Boolean isClosed
) {
}
