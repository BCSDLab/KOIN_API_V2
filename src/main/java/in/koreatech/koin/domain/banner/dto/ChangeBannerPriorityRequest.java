package in.koreatech.koin.domain.banner.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.*;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.banner.enums.PriorityChangeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record ChangeBannerPriorityRequest(
    @Schema(description = "변경 타입", example ="UP", requiredMode = REQUIRED)
    @NotNull(message = "변경 타입은 필수입니다.")
    PriorityChangeType changeType
) {
}
