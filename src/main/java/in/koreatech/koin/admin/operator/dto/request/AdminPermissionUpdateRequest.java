package in.koreatech.koin.admin.operator.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminPermissionUpdateRequest(
    @Schema(description = "어드민 생성 권한", example = "false", requiredMode = REQUIRED)
    @NotNull(message = "어드민 생성 권한은 필수 입력 사항입니다")
    Boolean canCreateAdmin,

    @Schema(description = "슈퍼 어드민 권한", example = "false", requiredMode = REQUIRED)
    @NotNull(message = "슈퍼 어드민 권한은 필수 입력 사항입니다")
    Boolean superAdmin
) {
}
