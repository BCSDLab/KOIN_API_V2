package in.koreatech.koin.admin.shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public record AdminModifyShopsCategoryRequest(
    @Schema(description = "이미지 URL", example = "https://static.koreatech.in/test.png", requiredMode = RequiredMode.REQUIRED)
    String imageUrl,

    @Schema(description = "이름", example = "햄버거", requiredMode = RequiredMode.REQUIRED)
    String name
) {

}
