package in.koreatech.koin.admin.shop.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminCreateShopsCategoryRequest(
    @Schema(description = "이미지 URL", example = "https://static.koreatech.in/test.png", requiredMode = RequiredMode.REQUIRED)
    String imageUrl,

    @Schema(description = "이름", example = "햄버거", requiredMode = RequiredMode.REQUIRED)
    String name
) {

}

