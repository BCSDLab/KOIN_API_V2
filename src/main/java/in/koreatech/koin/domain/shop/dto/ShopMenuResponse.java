package in.koreatech.koin.domain.shop.dto;

import java.util.List;

import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.MenuImage;
import in.koreatech.koin.domain.shop.model.MenuOption;

public record ShopMenuResponse(
    Integer count,
    List<InnerMenuCategoriesResponse> menuCategories
) {
    public static ShopMenuResponse createShopMenuResponse(List<MenuCategory> menuCategories) {
        return new ShopMenuResponse(
            menuCategories.size(),
            menuCategories.stream().map(InnerMenuCategoriesResponse::of).toList()
        );
    }

    public record InnerMenuCategoriesResponse(
        Long id,
        String name,
        List<InnerMenuResponse> menuResponses
    ) {
        public static InnerMenuCategoriesResponse of(MenuCategory menuCategory) {
            return new InnerMenuCategoriesResponse(
                menuCategory.getId(),
                menuCategory.getName(),
                menuCategory.getMenuCategoryMaps().stream().map(InnerMenuResponse::of).toList()
            );
        }

        public record InnerMenuResponse(
            String description,
            Long id,
            List<String> imageUrls,
            Boolean isHidden,
            Boolean isSingle,
            String name,
            List<InnerOptionPrice> optionPrices,
            Integer singlePrice
        ) {
            public static InnerMenuResponse of(MenuCategoryMap menuCategoryMap) {
                Menu menu = menuCategoryMap.getMenu();
                boolean isMultipleOption = menu.hasMultipleOption();
                return new InnerMenuResponse(
                    menu.getDescription(),
                    menu.getId(),
                    menu.getMenuImages().stream().map(MenuImage::getImageUrl).toList(),
                    menu.getIsHidden(),
                    isMultipleOption,
                    menu.getName(),
                    isMultipleOption? menu.getMenuOptions().stream().map(InnerOptionPrice::of).toList(): null,
                    isMultipleOption? menu.getMenuOptions().get(0).getPrice(): null
                );
            }

            public record InnerOptionPrice(
                String option,
                Integer price
            ) {
                public static InnerOptionPrice of(MenuOption menuOption) {
                    return new InnerOptionPrice(
                        menuOption.getOption(),
                        menuOption.getPrice()
                    );
                }
            }
        }
    }
}
