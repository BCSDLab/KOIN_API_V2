package in.koreatech.koin.domain.order.cart.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.cart.dto.CartAmountSummaryResponse;
import in.koreatech.koin.domain.order.cart.dto.CartMenuItemEditResponse;
import in.koreatech.koin.domain.order.cart.dto.CartPaymentSummaryResponse;
import in.koreatech.koin.domain.order.cart.dto.CartResponse;
import in.koreatech.koin.domain.order.cart.exception.CartErrorCode;
import in.koreatech.koin.domain.order.cart.exception.CartException;
import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import in.koreatech.koin.domain.order.model.OrderType;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.menu.OrderableShopMenuRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartQueryService {

    private final CartRepository cartRepository;
    private final OrderableShopMenuRepository orderableShopMenuRepository;

    public CartResponse getCartItems(Integer userId, OrderType orderType) {
        Optional<Cart> cartOptional = cartRepository.findCartByUserId(userId);

        if (cartOptional.isEmpty() || cartOptional.get().getCartMenuItems().isEmpty()) {
            return CartResponse.empty();
        }

        Cart cart = cartOptional.get();
        cart.validateOrderType(orderType);

        return CartResponse.from(cart, orderType);
    }

    public CartMenuItemEditResponse getOrderableShopMenuForEditOptions(Integer userId, Integer cartMenuItemId) {
        Cart cart = getCartOrThrow(userId);
        CartMenuItem cartMenuItem = cart.getCartMenuItem(cartMenuItemId);

        OrderableShopMenu menu = orderableShopMenuRepository.getByIdWithMenuOptionGroups(
            cartMenuItem.getOrderableShopMenu().getId());

        return CartMenuItemEditResponse.of(menu, cartMenuItem);
    }

    public CartAmountSummaryResponse getCartSummary(Integer userId, Integer orderableShopId) {
        Optional<Cart> cartOptional = cartRepository.findCartByUserId(userId);

        if (cartOptional.isEmpty() ||
            cartOptional.get().getCartMenuItems().isEmpty() ||
            !cartOptional.get().getOrderableShop().getId().equals(orderableShopId)
        ) {
            return CartAmountSummaryResponse.empty();
        }

        Cart cart = cartOptional.get();
        return CartAmountSummaryResponse.from(cart);
    }

    public CartPaymentSummaryResponse getCartPaymentSummary(Integer userId, OrderType orderType) {
        Optional<Cart> cartOptional = cartRepository.findCartByUserId(userId);

        if (cartOptional.isEmpty() || cartOptional.get().getCartMenuItems().isEmpty()) {
            return CartPaymentSummaryResponse.empty();
        }

        Cart cart = cartOptional.get();
        cart.validateOrderType(orderType);
        return CartPaymentSummaryResponse.from(cart, orderType);
    }

    public void validateCart(Integer userId) {
        Cart cart = getCartOrThrow(userId);
        OrderableShop orderableShop = cart.getOrderableShop();
        orderableShop.requireShopOpen();
        orderableShop.requireMinimumOrderAmount(cart.calculateItemsAmount());
    }

    private Cart getCartOrThrow(Integer userId) {
        return cartRepository.findCartByUserId(userId)
            .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));
    }
}
