package in.koreatech.koin.admin.shop.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminShopCategoryResponse(
    @Schema(description = "카테고리 고유 ID", example = "0")
    int id,

    @Schema(description = "카테고리 이미지 URL", example = "string")
    String imageUrl,

    @Schema(description = "카테고리 이름", example = "string")
    String name,

    @Schema(description = "상위 카테고리 ID", example = "1")
    Integer parentCategoryId
) {

    public static AdminShopCategoryResponse from(ShopCategory shopCategory) {
        return new AdminShopCategoryResponse(
            shopCategory.getId(),
            shopCategory.getImageUrl(),
            shopCategory.getName(),
            shopCategory.getParentCategory().getId()
        );
    }
}
