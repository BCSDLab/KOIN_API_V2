package in.koreatech.koin.unit.fixutre;

import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOptionGroup;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOptionGroupMap;
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

    public static OrderableShopMenu createSoldOutMenu(OrderableShop shop, String name, Integer menuId) {
        OrderableShopMenu menu = OrderableShopMenu.builder()
            .orderableShop(shop)
            .name(name)
            .description("메뉴")
            .isSoldOut(true)
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

    public static OrderableShopMenuOptionGroup createMenuOptionGroupWithEmptyMenuOption(
        OrderableShop shop, String name, Integer minSelect, Integer maxSelect, Boolean isRequired, Integer groupId
    ) {
        OrderableShopMenuOptionGroup optionGroup =  OrderableShopMenuOptionGroup.builder()
            .orderableShop(shop)
            .name(name)
            .maxSelect(maxSelect)
            .minSelect(minSelect)
            .description("옵션 그룹")
            .isDeleted(false)
            .isRequired(isRequired)
            .menuOptions(List.of())
            .build();

        ReflectionTestUtils.setField(optionGroup, "id", groupId);

        return optionGroup;
    }

    public static OrderableShopMenuOption createMenuOption(OrderableShopMenuOptionGroup group, String name, Integer price, Integer optionId) {
        OrderableShopMenuOption menuOption = OrderableShopMenuOption.builder()
            .optionGroup(group)
            .price(price)
            .name(name)
            .isDeleted(false)
            .build();
        ReflectionTestUtils.setField(menuOption, "id", optionId);

        return menuOption;
    }

    public static OrderableShopMenuOptionGroupMap createMenuOptionGroupMap(OrderableShopMenuOptionGroup group, OrderableShopMenu orderableShopMenu, Integer id) {
        OrderableShopMenuOptionGroupMap orderableShopMenuOptionGroupMap = OrderableShopMenuOptionGroupMap.builder()
            .optionGroup(group)
            .menu(orderableShopMenu)
            .isDeleted(false)
            .build();

        ReflectionTestUtils.setField(orderableShopMenuOptionGroupMap, "id", id);
        return orderableShopMenuOptionGroupMap;
    }
}
