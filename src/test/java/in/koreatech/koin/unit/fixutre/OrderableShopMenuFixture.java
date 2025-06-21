package in.koreatech.koin.unit.fixutre;

import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;

public class OrderableShopMenuFixture {

    private OrderableShopMenuFixture() {}

    public static OrderableShopMenu createMenu(OrderableShop shop, String name, Integer menuId) {
        OrderableShopMenu menu = OrderableShopMenu.builder()
            .orderableShop(shop)
            .name(name)
            .description("메뉴")
            .isSoldOut(false)
            .isDeleted(false)
            .build();

        ReflectionTestUtils.setField(menu, "id", menuId);

        return menu;
    }

    public static OrderableShopMenuPrice createMenuPrice(OrderableShopMenu menu, String name, Integer price, Integer priceId) {
        OrderableShopMenuPrice menuPrice = OrderableShopMenuPrice.builder()
            .price(price)
            .name(name)
            .menu(menu)
            .isDeleted(false)
            .build();

        ReflectionTestUtils.setField(menuPrice, "id", priceId);

        return menuPrice;
    }

    public static OrderableShopMenuOption createMenuOption(String name, Integer price, Integer optionId) {
        OrderableShopMenuOption menuOption = OrderableShopMenuOption.builder()
            .price(price)
            .name(name)
            .isDeleted(false)
            .build();
        ReflectionTestUtils.setField(menuOption, "id", optionId);

        return menuOption;
    }
}
