package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record RoleInput(
    @Schema(description = "역할명. 앞뒤 공백은 제거되어 저장됩니다.", example = "PM", requiredMode = REQUIRED)
    @NotBlank
    @Size(min = 1, max = 10)
    String name,

    @Schema(description = "역할 정원", example = "1", requiredMode = REQUIRED)
    @NotNull
    @Min(1)
    @Max(10)
    Integer maxParticipants
) {
    public RoleInput {
        name = RecruitmentRequestValidator.canonicalRoleName(name);
    }
}
