package in.koreatech.koin.domain.payment.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.cart.model.CartMenuItemOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import in.koreatech.koin.domain.payment.model.domain.TemporaryMenuItems;
import in.koreatech.koin.domain.payment.model.domain.TemporaryMenuOption;
import in.koreatech.koin.domain.payment.model.domain.TemporaryMenuPrice;

@Component
public class TemporaryMenuItemsMapper {

    public List<TemporaryMenuItems> fromCart(Cart cart) {
        return cart.getCartMenuItems().stream()
            .map(this::fromCartMenuItem)
            .toList();
    }

    private TemporaryMenuItems fromCartMenuItem(CartMenuItem cartMenuItem) {
        List<TemporaryMenuOption> options = cartMenuItem.getCartMenuItemOptions().stream()
            .map(this::fromCartMenuItemOption)
            .toList();

        OrderableShopMenuPrice price = cartMenuItem.getOrderableShopMenuPrice();

        return new TemporaryMenuItems(
            cartMenuItem.getOrderableShopMenu().getName(),
            cartMenuItem.getQuantity(),
            cartMenuItem.calculateTotalAmount(),
            new TemporaryMenuPrice(price.getName(), price.getPrice()),
            options
        );
    }

    private TemporaryMenuOption fromCartMenuItemOption(CartMenuItemOption option) {
        OrderableShopMenuOption shopOption = option.getOrderableShopMenuOption();
        String optionGroupName = shopOption.getOptionGroup().getName();

        return new TemporaryMenuOption(
            optionGroupName,
            option.getOptionName(),
            option.getQuantity(),
            option.getOptionPrice()
        );
    }
}
