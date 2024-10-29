package in.koreatech.koin.admin.user.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminUpdateRequest(
    @Schema(description = "이름", example = "신관규", requiredMode = REQUIRED)
    @NotBlank(message = "이름은 필수 입력 사항입니다.")
    String name,

    @Schema(description = "트랙 이름", example = "백엔드", requiredMode = REQUIRED)
    @NotBlank(message = "트랙 이름은 필수 입력 사항입니다.")
    String trackName,

    @Schema(description = "팀 이름", example = "유저", requiredMode = REQUIRED)
    @NotBlank(message = "팀 이름은 필수 입력 사항입니다.")
    String teamName
) {
}
