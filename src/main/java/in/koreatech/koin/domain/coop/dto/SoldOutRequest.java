package in.koreatech.koin.domain.coop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record SoldOutRequest(
    @Schema(description = "메뉴 고유 ID", example = "1", requiredMode = REQUIRED)
    @NotNull(message = "메뉴 ID는 필수입니다.")
    Integer menuId,

    @Schema(description = "품절 여부", example = "true", requiredMode = REQUIRED)
    @NotNull(message = "품절 여부는 필수입니다.")
    Boolean soldOut
) {

}
