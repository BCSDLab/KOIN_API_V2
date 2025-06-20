package in.koreatech.koin.domain.order.cart.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.cart.dto.CartAmountSummaryResponse;
import in.koreatech.koin.domain.order.cart.dto.CartResponse;
import in.koreatech.koin.domain.order.cart.dto.CartMenuItemEditResponse;
import in.koreatech.koin.domain.order.cart.exception.CartErrorCode;
import in.koreatech.koin.domain.order.cart.exception.CartException;
import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.cart.service.implement.CartGetter;
import in.koreatech.koin.domain.order.cart.service.implement.MenuGetter;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartQueryService {

    private final CartGetter cartGetter;
    private final MenuGetter menuGetter;

    public CartResponse getCartItems(Integer userId) {
        Optional<Cart> cartOptional = cartGetter.get(userId);

        if (cartOptional.isEmpty() || cartOptional.get().getCartMenuItems().isEmpty()) {
            return CartResponse.empty();
        }

        Cart cart = cartOptional.get();
        return CartResponse.from(cart);
    }

    public CartMenuItemEditResponse getOrderableShopMenuForEditOptions(Integer userId, Integer cartMenuItemId) {
        Cart cart = cartGetter.get(userId)
            .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));
        CartMenuItem cartMenuItem = cart.getCartMenuItem(cartMenuItemId);

        OrderableShopMenu menu = menuGetter.get(cartMenuItem.getOrderableShopMenu().getId());

        return CartMenuItemEditResponse.of(menu, cartMenuItem);
    }

    public CartAmountSummaryResponse getCartSummary(Integer userId, Integer orderableShopId) {
        Optional<Cart> cartOptional = cartGetter.get(userId);

        if (cartOptional.isEmpty() ||
            cartOptional.get().getCartMenuItems().isEmpty() ||
            !cartOptional.get().getOrderableShop().getId().equals(orderableShopId)
        ) {
            return CartAmountSummaryResponse.empty();
        }

        Cart cart = cartOptional.get();
        return CartAmountSummaryResponse.from(cart);
    }
}
