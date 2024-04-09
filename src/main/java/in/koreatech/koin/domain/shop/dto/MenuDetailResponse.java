package in.koreatech.koin.domain.shop.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.MenuImage;
import in.koreatech.koin.domain.shop.model.MenuOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@JsonNaming(value = SnakeCaseStrategy.class)
public record MenuDetailResponse(
    @Schema(example = "1", description = "고유id")
    Integer id,

    @Schema(example = "1", description = "메뉴가 소속된 상점의 고유 id ")
    Integer shopId,

    @Schema(example = "탕수육", description = "이름")
    String name,

    @Schema(example = "false", description = "숨김 여부")
    Boolean isHidden,

    @Schema(example = "false", description = "단일 메뉴 여부")
    Boolean isSingle,

    @Schema(example = "7000", description = "단일 메뉴일때(is_single이 true일때)의 가격")
    Integer singlePrice,

    @Schema(description = "옵션이 있는 메뉴일때(is_single이 false일때)의 가격")
    List<InnerOptionPriceResponse> optionPrices,

    @Schema(example = "돼지고기 + 튀김", description = "구성 설명")
    String description,

    @Schema(description = "소속되어 있는 메뉴 카테고리 고유 id 리스트")
    List<Integer> categoryIds,

    @Schema(description = "이미지 URL 리스트")
    List<String> imageUrls
) {

    public static MenuDetailResponse createForSingleOption(Menu menu, List<MenuCategory> shopMenuCategories) {
        if (menu.hasMultipleOption()) {
            log.warn("{}는 옵션이 하나 이상인 메뉴입니다. createForMultipleOption 메서드를 이용해야 합니다.", menu);
            throw new IllegalStateException("서버에 에러가 발생했습니다.");
        }

        return new MenuDetailResponse(
            menu.getId(),
            menu.getShopId(),
            menu.getName(),
            menu.isHidden(),
            true,
            menu.getMenuOptions().get(0).getPrice(),
            null,
            menu.getDescription(),
            shopMenuCategories.stream().map(MenuCategory::getId).toList(),
            menu.getMenuImages().stream().map(MenuImage::getImageUrl).toList()
        );
    }

    public static MenuDetailResponse createMenuDetailResponse(Menu menu, List<MenuCategory> menuCategories) {
        if (menu.hasMultipleOption()) {
            return MenuDetailResponse.createForMultipleOption(menu, menuCategories);
        }
        return MenuDetailResponse.createForSingleOption(menu, menuCategories);
    }

    public static MenuDetailResponse createForMultipleOption(Menu menu, List<MenuCategory> shopMenuCategories) {
        if (!menu.hasMultipleOption()) {
            log.error("{}는 옵션이 하나인 메뉴입니다. createForSingleOption 메서드를 이용해야 합니다.", menu);
            throw new IllegalStateException("서버에 에러가 발생했습니다.");
        }

        return new MenuDetailResponse(
            menu.getId(),
            menu.getShopId(),
            menu.getName(),
            menu.isHidden(),
            false,
            null,
            menu.getMenuOptions().stream().map(InnerOptionPriceResponse::of).toList(), menu.getDescription(),
            shopMenuCategories.stream().map(MenuCategory::getId).toList(),
            menu.getMenuImages().stream().map(MenuImage::getImageUrl).toList()
        );
    }

    private record InnerOptionPriceResponse(
        @Schema(example = "소", description = "옵션명")
        String option,

        @Schema(example = "10000", description = "옵션에 대한 가격")
        Integer price
    ) {
        public static InnerOptionPriceResponse of(MenuOption menuOption) {
            return new InnerOptionPriceResponse(menuOption.getOption(), menuOption.getPrice());
        }
    }
}
