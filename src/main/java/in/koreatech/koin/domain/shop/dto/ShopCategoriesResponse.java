package in.koreatech.koin.domain.shop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.ShopCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopCategoriesResponse(
    @Schema(example = "10", description = "상점 카테고리 개수", requiredMode = REQUIRED)
    Integer totalCount,

    @Schema(description = "모든 상점 카테고리 리스트", requiredMode = NOT_REQUIRED)
    List<InnerShopCategory> shopCategories
) {

    public static ShopCategoriesResponse from(List<ShopCategory> shopCategories) {
        return new ShopCategoriesResponse(
            shopCategories.size(),
            shopCategories.stream().map(InnerShopCategory::from).toList()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerShopCategory(
        @Schema(example = "2", description = "고유 id", requiredMode = REQUIRED)
        Integer id,

        @Schema(example = "https://static.koreatech.in/test.png", description = "이미지 URL", requiredMode = NOT_REQUIRED)
        String imageUrl,

        @Schema(example = "치킨", description = "이름", requiredMode = REQUIRED)
        String name
    ) {

        public static InnerShopCategory from(ShopCategory shopCategory) {
            return new InnerShopCategory(
                shopCategory.getId(),
                shopCategory.getImageUrl(),
                shopCategory.getName()
            );
        }
    }
}
