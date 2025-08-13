package in.koreatech.koin.unit.fixture;

import java.util.List;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.cart.model.CartMenuItemOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.user.model.User;

public class CartFixture {

    private CartFixture() {}

    public static Cart createCart(User user, OrderableShop orderableShop) {
        return Cart.builder()
            .user(user)
            .orderableShop(orderableShop)
            .build();
    }

    public static CartMenuItem cartMenuItem(
        Cart cart, OrderableShopMenu menu, OrderableShopMenuPrice price,
        List<OrderableShopMenuOption> options, Integer quantity
    ) {
        CartMenuItem cartMenuItem = CartMenuItem.builder()
            .cart(cart)
            .orderableShopMenu(menu)
            .orderableShopMenuPrice(price)
            .quantity(quantity)
            .isModified(false)
            .build();

        if (options != null) {
            options.forEach(option ->
                cartMenuItem.getCartMenuItemOptions().add(CartMenuItemOption.create(cartMenuItem, option))
            );
        }
        return cartMenuItem;
    }
}
