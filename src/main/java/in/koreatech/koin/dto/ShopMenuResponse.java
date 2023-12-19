package in.koreatech.koin.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.Menu;
import in.koreatech.koin.domain.shop.MenuCategory;
import in.koreatech.koin.domain.shop.MenuImage;
import in.koreatech.koin.domain.shop.MenuOption;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonNaming(value = SnakeCaseStrategy.class)
public class ShopMenuResponse {

    private final Long id;
    private final Long shopId;
    private final String name;
    private final Boolean isHidden;
    private final Boolean isSingle;
    private final Integer singlePrice;
    private final List<InnerOptionPriceResponse> optionPrices;
    private final String description;
    private final List<Long> categoryIds;
    private final List<String> imageUrls;

    public static ShopMenuResponse of(Menu menu, List<MenuCategory> shopMenuCategories) {
        return new ShopMenuResponse(
            menu.getId().longValue(),
            menu.getShopId(),
            menu.getName(),
            menu.getIsHidden(),
            menu.getMenuOptions().size() == 1,
            menu.getMenuOptions().size() == 1 ? menu.getMenuOptions().get(0).getPrice() : null,
            menu.getMenuOptions().size() == 1 ? null : menu.getMenuOptions().stream()
                .map(InnerOptionPriceResponse::of)
                .toList(),
            menu.getDescription(),
            shopMenuCategories.stream()
                .map(MenuCategory::getId)
                .toList(),
            menu.getMenuImages().stream()
                .map(MenuImage::getImageUrl)
                .toList()
        );
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class InnerOptionPriceResponse {
        private final String option;
        private final Integer price;

        public static InnerOptionPriceResponse of(MenuOption menuOption) {
            return new InnerOptionPriceResponse(menuOption.getOption(), menuOption.getPrice());
        }
    }
}
