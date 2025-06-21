package in.koreatech.koin.unit.fixutre;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.user.model.User;

public class CartFixture {

    private CartFixture() {}

    public static Cart createCart(User user, OrderableShop orderableShop) {
        return Cart.from(user, orderableShop);
    }
}
