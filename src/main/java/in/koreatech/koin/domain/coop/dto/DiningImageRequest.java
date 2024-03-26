package in.koreatech.koin.domain.coop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record DiningImageRequest(
    @Schema(description = "메뉴 고유 ID", example = "1")
    Long menuId,

    @Schema(description = "이미지 url", example = "https://api.koreatech.in/image.jpg")
    String imageUrl
) {

}
