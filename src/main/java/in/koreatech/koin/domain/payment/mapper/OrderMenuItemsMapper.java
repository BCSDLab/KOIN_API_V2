package in.koreatech.koin.domain.payment.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.cart.model.CartMenuItemOption;
import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.domain.order.model.OrderMenu;
import in.koreatech.koin.domain.order.model.OrderMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOptionGroup;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;

@Component
public class OrderMenuItemsMapper {

    public List<OrderMenu> toOrderMenus(Cart cart, Order order) {
        return cart.getCartMenuItems().stream()
            .map(cartMenuItem -> toOrderMenu(cartMenuItem, order))
            .toList();
    }

    private OrderMenu toOrderMenu(CartMenuItem cartMenuItem, Order order) {
        OrderableShopMenuPrice price = cartMenuItem.getOrderableShopMenuPrice();
        OrderMenu orderMenu = OrderMenu.builder()
            .menuName(cartMenuItem.getOrderableShopMenu().getName())
            .menuPrice(price.getPrice())
            .menuPriceName(price.getName())
            .quantity(cartMenuItem.getQuantity())
            .isDeleted(false)
            .order(order)
            .orderableShopMenu(cartMenuItem.getOrderableShopMenu())
            .orderableShopMenuPrice(price)
            .build();

        List<OrderMenuOption> orderMenuOptions = cartMenuItem.getCartMenuItemOptions().stream()
            .map(cartMenuItemOption -> toOrderMenuOption(cartMenuItemOption, orderMenu))
            .toList();

        orderMenu.setOrderMenuOptions(orderMenuOptions);
        return orderMenu;
    }

    private OrderMenuOption toOrderMenuOption(CartMenuItemOption option, OrderMenu orderMenu) {
        OrderableShopMenuOption shopOption = option.getOrderableShopMenuOption();
        OrderableShopMenuOptionGroup optionGroup = shopOption.getOptionGroup();

        return OrderMenuOption.builder()
            .optionGroupName(optionGroup.getName())
            .optionName(option.getOptionName())
            .optionPrice(option.getOptionPrice())
            .quantity(option.getQuantity())
            .isDeleted(false)
            .orderMenu(orderMenu)
            .orderableShopMenuOption(shopOption)
            .orderableShopMenuOptionGroup(shopOption.getOptionGroup())
            .build();
    }
}
