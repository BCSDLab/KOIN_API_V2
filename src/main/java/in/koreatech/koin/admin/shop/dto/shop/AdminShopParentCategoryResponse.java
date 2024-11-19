package in.koreatech.koin.admin.shop.dto.shop;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminShopParentCategoryResponse(
    @Schema(description = "상위 카테고리 고유 ID", example = "1")
    int id,

    @Schema(description = "상위 카테고리 이름", example = "가게")
    String name
) {

    public static AdminShopParentCategoryResponse from(ShopParentCategory shopParentCategory) {
        return new AdminShopParentCategoryResponse(
            shopParentCategory.getId(),
            shopParentCategory.getName()
        );
    }
}
