package in.koreatech.koin.admin.abtest.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record AbtestAssignRequest(
    @NotNull(message = "테스트 변수명은 필수입니다.")
    @Schema(description = "테스트 변수명", example = "dining_ui_test", requiredMode = REQUIRED)
    String title
) {
}
