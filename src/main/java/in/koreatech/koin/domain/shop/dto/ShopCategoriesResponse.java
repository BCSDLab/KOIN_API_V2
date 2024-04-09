package in.koreatech.koin.domain.shop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.ShopCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopCategoriesResponse(
    @Schema(example = "10", description = "개수")
    Integer totalCount,

    @Schema(description = "모든 상점 카테고리 리스트")
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
        @Schema(example = "2", description = "고유 id")
        Integer id,

        @Schema(example = "https://static.koreatech.in/test.png", description = "이미지 URL")
        String imageUrl,

        @Schema(example = "치킨", description = "이름")
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
