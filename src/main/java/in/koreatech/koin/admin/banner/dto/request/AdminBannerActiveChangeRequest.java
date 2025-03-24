package in.koreatech.koin.admin.banner.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminBannerActiveChangeRequest(
    @Schema(description = "활성화 여부", example ="true", requiredMode = REQUIRED)
    @NotNull(message = "활성화 여부는 필수입니다.")
    Boolean isActive
) {
}
