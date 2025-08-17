package in.koreatech.koin.admin.manager.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.manager.enums.TeamType;
import in.koreatech.koin.admin.manager.enums.TrackType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminUpdateRequest(
    @Schema(description = "이름", example = "신관규", requiredMode = REQUIRED)
    @NotBlank(message = "이름은 필수 입력 사항입니다.")
    String name,

    @Schema(description = "트랙 타입", example = "BACKEND", requiredMode = REQUIRED)
    @NotNull(message = "트랙 타입은 필수 입력 사항입니다.")
    TrackType trackType,

    @Schema(description = "팀 타입", example = "USER", requiredMode = REQUIRED)
    @NotNull(message = "팀 타입은 필수 입력 사항입니다.")
    TeamType teamType
) {
}
