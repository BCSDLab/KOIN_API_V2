package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record CreateApplicationRequest(
    @Schema(
        description = "역할 기반 모집은 열린 역할 ID, 일반 모집은 null",
        example = "1",
        nullable = true,
        requiredMode = REQUIRED
    )
    @JsonProperty(value = "role_id", required = true)
    Integer roleId,

    @Schema(description = "지원 동기", example = "지원 동기입니다.", requiredMode = REQUIRED)
    @NotBlank(message = "지원 동기는 필수입니다.")
    @Size(max = 1000, message = "지원 동기는 1000자 이하여야 합니다.")
    String motivation,

    @Schema(description = "가능 시간", example = "월수금 20시 이후", requiredMode = REQUIRED)
    @NotBlank(message = "가능 시간은 필수입니다.")
    @Size(max = 100, message = "가능 시간은 100자 이하여야 합니다.")
    String availability
) {
}
