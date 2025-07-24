package in.koreatech.koin.unit.fixutre;

import java.util.List;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;

public class CartMenuItemFixture {

    private CartMenuItemFixture() {}

    public static CartMenuItem createCartMenuItemWithoutOption(
        Cart cart, OrderableShopMenu menu, OrderableShopMenuPrice menuPrice
    ) {
        return CartMenuItem.create(cart, menu, menuPrice, List.of());
    }
}
