package in.koreatech.koin.domain.shop.dto.shop.response;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.language.service.ContentMessageCatalog;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopCategoriesResponse(
    @Schema(example = "10", description = "상점 카테고리 개수", requiredMode = REQUIRED)
    Integer totalCount,

    @Schema(description = "모든 상점 카테고리 리스트", requiredMode = NOT_REQUIRED)
    List<InnerShopCategory> shopCategories
) {
    public ShopCategoriesResponse localized(SupportedLanguage language) {
        return new ShopCategoriesResponse(totalCount, shopCategories.stream().map(category -> category.localized(language)).toList());
    }


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
        String name,

        @Schema(description = "선택 언어의 표시명. 번역이 없으면 원문을 사용합니다.")
        String displayName
    ) {
        public InnerShopCategory(
            Integer id,
            String imageUrl,
            String name
        ) {
            this(id, imageUrl, name, name);
        }

        public InnerShopCategory localized(SupportedLanguage language) {
            return new InnerShopCategory(id, imageUrl, name, ContentMessageCatalog.text(language, "shop.category." + name, name));
        }


        public static InnerShopCategory from(ShopCategory shopCategory) {
            return new InnerShopCategory(
                shopCategory.getId(),
                shopCategory.getImageUrl(),
                shopCategory.getName()
            );
        }
    }
}
