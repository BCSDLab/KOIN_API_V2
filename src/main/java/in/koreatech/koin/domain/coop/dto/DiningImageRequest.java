package in.koreatech.koin.domain.coop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record DiningImageRequest(
    @Schema(description = "메뉴 고유 ID", example = "1", requiredMode = REQUIRED)
    @NotNull(message = "메뉴 ID는 필수입니다.")
    Integer menuId,

    @Schema(description = "이미지 url", example = "https://api.koreatech.in/image.jpg", requiredMode = REQUIRED)
    @NotEmpty(message = "메뉴 이미지는 필수입니다.")
    String imageUrl
) {

}
