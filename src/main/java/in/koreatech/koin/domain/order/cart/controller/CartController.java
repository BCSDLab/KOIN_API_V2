package in.koreatech.koin.domain.order.cart.controller;

import static in.koreatech.koin.domain.user.model.UserType.GENERAL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.domain.order.cart.dto.CartAddItemRequest;
import in.koreatech.koin.domain.order.cart.dto.CartItemsResponse;
import in.koreatech.koin.domain.order.cart.dto.CartMenuItemEditResponse;
import in.koreatech.koin.domain.order.cart.dto.CartUpdateItemRequest;
import in.koreatech.koin.domain.order.cart.service.CartQueryService;
import in.koreatech.koin.domain.order.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CartController implements CartApi {

    private final CartService cartService;
    private final CartQueryService cartQueryService;

    @GetMapping("/cart")
    public ResponseEntity<CartItemsResponse> getCartItems(
        @Auth(permit = {GENERAL, STUDENT}) Integer userId
    ) {
        CartItemsResponse response = cartQueryService.getCartItems(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cart/add")
    public ResponseEntity<Void> addItem(
        @RequestBody @Valid CartAddItemRequest cartAddItemRequest,
        @Auth(permit = {GENERAL, STUDENT}) Integer userId
    ) {
        cartService.addItem(cartAddItemRequest.toCommand(userId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart/quantity/{cartMenuItemId}/{quantity}")
    public ResponseEntity<Void> updateItemQuantity(
        @PathVariable Integer cartMenuItemId,
        @PathVariable Integer quantity,
        @Auth(permit = {GENERAL, STUDENT}) Integer userId
    ) {
        cartService.updateItemQuantity(userId, cartMenuItemId, quantity);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cart/delete/{cartMenuItemId}")
    public ResponseEntity<Void> deleteItem(
        @PathVariable Integer cartMenuItemId,
        @Auth(permit = {GENERAL, STUDENT}) Integer userId
    ) {
        cartService.deleteItem(userId, cartMenuItemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cart/reset")
    public ResponseEntity<Void> reset(
        @Auth(permit = {GENERAL, STUDENT}) Integer userId
    ) {
        cartService.resetCart(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/cart/item/{cartMenuItemId}")
    public ResponseEntity<Void> updateCartItem(
        @PathVariable Integer cartMenuItemId,
        @RequestBody @Valid CartUpdateItemRequest request,
        @Auth(permit = {GENERAL, STUDENT}) Integer userId
    ) {
        cartService.updateItem(request, cartMenuItemId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cart/item/{cartMenuItemId}/edit")
    public ResponseEntity<CartMenuItemEditResponse> getCartItemForEdit(
        @PathVariable Integer cartMenuItemId,
        @Auth(permit = {GENERAL, STUDENT}) Integer userId
    ) {
        CartMenuItemEditResponse response = cartQueryService.getOrderableShopMenuForEditOptions(userId, cartMenuItemId);
        return ResponseEntity.ok(response);
    }
}
