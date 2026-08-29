package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ApplicationRole(
    @Schema(description = "역할 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "역할명", example = "프론트엔드", requiredMode = REQUIRED)
    String name
) {
}
