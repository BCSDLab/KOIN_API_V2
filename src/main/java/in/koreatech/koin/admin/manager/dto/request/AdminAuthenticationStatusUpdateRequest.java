package in.koreatech.koin.admin.manager.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminAuthenticationStatusUpdateRequest(
    @Schema(description = "인증 상태", example = "true", requiredMode = REQUIRED)
    @NotNull(message = "인증 상태는 필수 입력 사항입니다.")
    Boolean isAuthed
) {
}
