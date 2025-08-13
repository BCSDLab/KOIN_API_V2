package in.koreatech.koin.unit.fixture;

import java.util.List;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;

public class CartMenuItemFixture {

    private CartMenuItemFixture() {}

    public static CartMenuItem createCartMenuItemWithoutOption(
        Cart cart, OrderableShopMenu menu, OrderableShopMenuPrice menuPrice, Integer quantity
    ) {
        return CartMenuItem.create(cart, menu, menuPrice, List.of(), quantity);
    }
}
