package in.koreatech.koin.domain.payment.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.cart.model.CartMenuItemOption;
import in.koreatech.koin.domain.order.model.OrderMenu;
import in.koreatech.koin.domain.order.model.OrderMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOptionGroup;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;

@Component
public class OrderMenuItemsMapper {

    public List<OrderMenu> fromCart(Cart cart) {
        return cart.getCartMenuItems().stream()
            .map(this::fromCartMenuItem)
            .toList();
    }

    private OrderMenu fromCartMenuItem(CartMenuItem cartMenuItem) {
        List<OrderMenuOption> options = cartMenuItem.getCartMenuItemOptions().stream()
            .map(this::fromCartMenuItemOption)
            .toList();

        OrderableShopMenuPrice price = cartMenuItem.getOrderableShopMenuPrice();

        return OrderMenu.builder()
            .menuName(cartMenuItem.getOrderableShopMenu().getName())
            .menuPrice(price.getPrice())
            .menuPriceName(price.getName())
            .quantity(cartMenuItem.getQuantity())
            .isDeleted(false)
            .orderableShopMenu(cartMenuItem.getOrderableShopMenu())
            .orderMenuOptions(options)
            .build();
    }

    private OrderMenuOption fromCartMenuItemOption(CartMenuItemOption option) {
        OrderableShopMenuOption shopOption = option.getOrderableShopMenuOption();
        OrderableShopMenuOptionGroup optionGroup = shopOption.getOptionGroup();

        return OrderMenuOption.builder()
            .optionGroupName(optionGroup.getName())
            .optionName(option.getOptionName())
            .optionPrice(option.getOptionPrice())
            .quantity(option.getQuantity())
            .isDeleted(false)
            .orderableShopMenuOption(shopOption)
            .orderableShopMenuOptionGroup(shopOption.getOptionGroup())
            .build();
    }
}
