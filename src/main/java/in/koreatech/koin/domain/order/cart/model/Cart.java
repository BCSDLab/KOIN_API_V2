package in.koreatech.koin.domain.order.cart.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import in.koreatech.koin._common.model.BaseEntity;
import in.koreatech.koin.domain.order.cart.exception.CartErrorCode;
import in.koreatech.koin.domain.order.cart.exception.CartException;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "cart")
@NoArgsConstructor(access = PROTECTED)
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderable_shop_id", nullable = false)
    private OrderableShop orderableShop;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    List<CartMenuItem> cartMenuItems = new ArrayList<>();

    public void validateSameShop(Integer orderableShopId) {
        if (!orderableShop.getId().equals(orderableShopId)) {
            throw new CartException(CartErrorCode.DIFFERENT_SHOP_ITEM_IN_CART);
        }
    }

    public void addItem(OrderableShopMenu menu, OrderableShopMenuPrice price, List<OrderableShopMenuOption> options) {
        // 장바구니에 옵션 까지 전부 동일한 메뉴가 이미 존재 하는 경우, 수량 1 증가
        Optional<CartMenuItem> existingItem = findSameItem(menu, price, options);

        if (existingItem.isPresent()) {
            existingItem.get().increaseQuantity();
        } else {
            CartMenuItem newItem = CartMenuItem.create(this, menu, price, options);
            this.cartMenuItems.add(newItem);
        }
    }

    public void updateItemQuantity(Integer cartMenuItemId, Integer quantity) {
        CartMenuItem existingItem = getCartMenuItem(cartMenuItemId);
        existingItem.updateQuantity(quantity);
    }

    public void removeItem(Integer cartMenuItemId) {
        CartMenuItem itemToRemove = getCartMenuItem(cartMenuItemId);
        this.cartMenuItems.remove(itemToRemove);
    }

    private Optional<CartMenuItem> findSameItem(OrderableShopMenu menu, OrderableShopMenuPrice price,
        List<OrderableShopMenuOption> options) {
        return this.cartMenuItems.stream()
            .filter(item -> item.isSameItem(menu, price, options))
            .findFirst();
    }

    public Optional<CartMenuItem> findSameItem(OrderableShopMenu menu, OrderableShopMenuPrice price,
        List<OrderableShopMenuOption> options, Integer excludeCartMenuItemId) {
        return this.cartMenuItems.stream()
            .filter(item -> !item.getId().equals(excludeCartMenuItemId))
            .filter(item -> item.isSameItem(menu, price, options))
            .findFirst();
    }

    public CartMenuItem getCartMenuItem(Integer cartMenuItemId) {
        return this.cartMenuItems.stream()
            .filter(cartMenuItem -> cartMenuItem.getId().equals(cartMenuItemId))
            .findFirst()
            .orElseThrow(() -> new CartException(CartErrorCode.CART_MENU_ITEM_NOT_FOUND));
    }

    private Cart(
        User user,
        OrderableShop orderableShop
    ) {
        this.user = user;
        this.orderableShop = orderableShop;
    }

    public static Cart from(User user, OrderableShop orderableShop) {
        return new Cart(user, orderableShop);
    }
}
