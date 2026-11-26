package in.koreatech.koin.domain.ownershop.model;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.menu.Menu;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import in.koreatech.koin.domain.shop.model.menu.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.menu.MenuImage;

@JsonNaming(SnakeCaseStrategy.class)
public record ShopOrderServiceMenuSnapshot(
    Integer menuId,
    String name,
    String description,
    List<Category> categories,
    List<Price> prices,
    List<String> imageUrls,
    List<OptionGroup> optionGroups
) {

    public static ShopOrderServiceMenuSnapshot from(Menu menu, List<OptionGroup> optionGroups) {
        List<Category> categories = menu.getMenuCategoryMaps().stream()
            .map(MenuCategoryMap::getMenuCategory)
            .map(Category::from)
            .toList();
        boolean hasMultiplePrices = menu.hasMultipleOption();
        List<Price> prices = menu.getMenuOptions().stream()
            .map(option -> new Price(hasMultiplePrices ? option.getOption() : null, option.getPrice()))
            .toList();
        return new ShopOrderServiceMenuSnapshot(
            menu.getId(),
            menu.getName(),
            menu.getDescription(),
            categories,
            prices,
            menu.getMenuImages().stream().map(MenuImage::getImageUrl).toList(),
            optionGroups
        );
    }

    public record Category(Integer id, String name) {

        private static Category from(MenuCategory category) {
            return new Category(category.getId(), category.getName());
        }
    }

    public record Price(String name, Integer price) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record OptionGroup(
        String name,
        String description,
        Boolean isRequired,
        Integer minSelect,
        Integer maxSelect,
        List<Option> options
    ) {
    }

    public record Option(String name, Integer price) {
    }
}
