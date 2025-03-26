package in.koreatech.koin.admin.banner.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.banner.enums.PriorityChangeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminBannerPriorityChangeRequest(
    @Schema(description = "변경 타입", example = "UP", requiredMode = REQUIRED)
    @NotNull(message = "변경 타입은 필수입니다.")
    PriorityChangeType changeType
) {
}
