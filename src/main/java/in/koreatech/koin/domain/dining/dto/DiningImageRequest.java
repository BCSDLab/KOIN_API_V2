package in.koreatech.koin.domain.dining.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DiningImageRequest(
    @Schema(description = "메뉴 고유 ID", example = "1")
    Long id,

    @Schema(description = "이미지 url", example = "https://api.koreatech.in/image.jpg")
    String imageUrl
) {

}
