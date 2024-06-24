package in.koreatech.koin.admin.shop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.ShopCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminShopCategoriesResponse(
    @Schema(description = "총 상점 카테고리 수", example = "57", requiredMode = REQUIRED)
    Integer totalCount,

    @Schema(description = "현재 페이지에서 조회된 상점 카테고리 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "전체 페이지 수", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "모든 상점 카테고리 리스트", requiredMode = NOT_REQUIRED)
    List<InnerShopCategory> categories
) {

    public static AdminShopCategoriesResponse of(List<ShopCategory> shopCategories, int currentPage, int totalPage) {
        return new AdminShopCategoriesResponse(
            shopCategories.size(),
            shopCategories.size(),
            totalPage,
            currentPage,
            shopCategories.stream().map(InnerShopCategory::from).toList()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopCategory(
        @Schema(description = "고유 id", example = "0", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "이미지 URL", example = "https://static.koreatech.in/test.png", requiredMode = NOT_REQUIRED)
        String imageUrl,

        @Schema(description = "이름", example = "치킨", requiredMode = REQUIRED)
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
