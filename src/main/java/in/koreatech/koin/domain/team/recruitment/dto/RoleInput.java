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

    @Schema(description = "해당 역할로 모집할 지원자 정원", example = "1", requiredMode = REQUIRED)
    @NotNull
    @Min(1)
    @Max(10)
    Integer maxParticipants
) {
    public RoleInput {
        name = RecruitmentRequestValidator.canonicalRoleName(name);
    }

    /**
     * 교차 검증에 필요한 값이 모두 들어왔는지 확인한다.
     * 비어 있으면 Bean Validation 이 400 을 반환하므로 교차 검증은 건너뛴다.
     */
    boolean isComplete() {
        return name != null && maxParticipants != null;
    }
}
