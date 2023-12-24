package in.koreatech.koin.dto.shop;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.Menu;
import in.koreatech.koin.domain.shop.MenuCategory;
import in.koreatech.koin.domain.shop.MenuImage;
import in.koreatech.koin.domain.shop.MenuOption;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopMenuResponse(Long id, Long shopId, String name, Boolean isHidden, Boolean isSingle,
                               Integer singlePrice, List<InnerOptionPriceResponse> optionPrices, String description,
                               List<Long> categoryIds, List<String> imageUrls) {

    public static ShopMenuResponse createForSingleOption(Menu menu, List<MenuCategory> shopMenuCategories) {
        if (menu.hasMultipleOption()) {
            log.warn("{}는 옵션이 하나 이상인 메뉴입니다. createForMultipleOption 메서드를 이용해야 합니다.", menu);
            throw new IllegalStateException("서버에 에러가 발생했습니다.");
        }

        return new ShopMenuResponse(
            menu.getId(),
            menu.getShopId(),
            menu.getName(),
            menu.getIsHidden(),
            true,
            menu.getMenuOptions().get(0).getPrice(),
            null,
            menu.getDescription(),
            shopMenuCategories.stream().map(MenuCategory::getId).toList(),
            menu.getMenuImages().stream().map(MenuImage::getImageUrl).toList()
        );
    }

    public static ShopMenuResponse createForMultipleOption(Menu menu, List<MenuCategory> shopMenuCategories) {
        if (!menu.hasMultipleOption()) {
            log.error("{}는 옵션이 하나인 메뉴입니다. createForSingleOption 메서드를 이용해야 합니다.", menu);
            throw new IllegalStateException("서버에 에러가 발생했습니다.");
        }

        return new ShopMenuResponse(
            menu.getId(),
            menu.getShopId(),
            menu.getName(),
            menu.getIsHidden(),
            false,
            null,
            menu.getMenuOptions().stream().map(InnerOptionPriceResponse::of).toList(), menu.getDescription(),
            shopMenuCategories.stream().map(MenuCategory::getId).toList(),
            menu.getMenuImages().stream().map(MenuImage::getImageUrl).toList()
        );
    }

    private record InnerOptionPriceResponse(String option, Integer price) {
        public static InnerOptionPriceResponse of(MenuOption menuOption) {
            return new InnerOptionPriceResponse(menuOption.getOption(), menuOption.getPrice());
        }
    }
}
