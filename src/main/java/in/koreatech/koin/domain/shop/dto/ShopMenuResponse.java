package in.koreatech.koin.domain.shop.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.MenuImage;
import in.koreatech.koin.domain.shop.model.MenuOption;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopMenuResponse(
    @Schema(example = "20", description = "개수")
    Integer count,

    @Schema(description = "카테고리 별로 분류된 소속 메뉴 리스트")
    List<InnerMenuCategoriesResponse> menuCategories,

    @Schema(example = "2024-03-16", description = "해당 상점 마지막 메뉴 업데이트 날짜")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDateTime updatedAt
) {
    public static ShopMenuResponse from(List<MenuCategory> menuCategories) {
        LocalDateTime lastUpdatedDate = LocalDateTime.of(1900, 1, 1, 0, 0);
        int count = 0;
        for (MenuCategory menuCategory : menuCategories) {
            for (MenuCategoryMap menuCategoryMap : menuCategory.getMenuCategoryMaps()) {
                LocalDateTime updatedAt = menuCategoryMap.getMenu().getUpdatedAt();
                if (updatedAt.isAfter(lastUpdatedDate)) {
                    lastUpdatedDate = updatedAt;
                }
                ++count;
            }
        }
        return new ShopMenuResponse(
            count,
            menuCategories.stream()
                .filter(menuCategory -> menuCategory.getMenuCategoryMaps().size() > 0)
                .map(InnerMenuCategoriesResponse::from).toList(),
            lastUpdatedDate
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerMenuCategoriesResponse(
        @Schema(example = "1", description = "카테고리 id")
        Long id,

        @Schema(example = "중식", description = "카테고리 이름")
        String name,

        @Schema(description = "해당 상점의 모든 메뉴 리스트")
        List<InnerMenuResponse> menus
    ) {
        public static InnerMenuCategoriesResponse from(MenuCategory menuCategory) {
            return new InnerMenuCategoriesResponse(
                menuCategory.getId(),
                menuCategory.getName(),
                menuCategory.getMenuCategoryMaps().stream().map(InnerMenuResponse::from).toList()
            );
        }

        @JsonNaming(value = SnakeCaseStrategy.class)
        private record InnerMenuResponse(
            @Schema(example = "1", description = "고유 id")
            Long id,

            @Schema(example = "탕수육", description = "이름")
            String name,

            @Schema(example = "false", description = "숨김 여부")
            Boolean isHidden,

            @Schema(example = "false", description = "단일 메뉴 여부")
            Boolean isSingle,

            @Schema(example = "10000", description = "단일 메뉴일때(is_single이 true일때)의 가격")
            Integer singlePrice,

            @Schema(description = "옵션이 있는 메뉴일때(is_single이 false일때)의 옵션에 따른 가격 리스트")
            List<InnerOptionPrice> optionPrices,

            @Schema(example = "저희 식당의 대표 메뉴 탕수육입니다.", description = "설명")
            String description,

            @Schema(description = "이미지 URL리스트")
            List<String> imageUrls
        ) {
            public static InnerMenuResponse from(MenuCategoryMap menuCategoryMap) {
                Menu menu = menuCategoryMap.getMenu();
                boolean isSingle = !menu.hasMultipleOption();
                return new InnerMenuResponse(
                    menu.getId(),
                    menu.getName(),
                    menu.getIsHidden(),
                    isSingle,
                    isSingle ? menu.getMenuOptions().get(0).getPrice() : null,
                    isSingle ? null : menu.getMenuOptions().stream().map(InnerOptionPrice::from).toList(),
                    menu.getDescription(),
                    menu.getMenuImages().stream().map(MenuImage::getImageUrl).toList()
                );
            }

            @JsonNaming(value = SnakeCaseStrategy.class)
            private record InnerOptionPrice(
                @Schema(example = "대", description = "옵션명")
                String option,

                @Schema(example = "26000", description = "가격")
                Integer price
            ) {
                public static InnerOptionPrice from(MenuOption menuOption) {
                    return new InnerOptionPrice(
                        menuOption.getOption(),
                        menuOption.getPrice()
                    );
                }
            }
        }
    }
}
