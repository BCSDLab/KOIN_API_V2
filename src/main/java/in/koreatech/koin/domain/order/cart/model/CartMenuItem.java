package in.koreatech.koin.domain.order.cart.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin._common.model.BaseEntity;
import in.koreatech.koin.domain.order.cart.exception.CartErrorCode;
import in.koreatech.koin.domain.order.cart.exception.CartException;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "cart_menu_item")
@NoArgsConstructor(access = PROTECTED)
public class CartMenuItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false, unique = true)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderable_shop_menu_id", nullable = false)
    private OrderableShopMenu orderableShopMenu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderable_shop_menu_price_id", nullable = false)
    private OrderableShopMenuPrice orderableShopMenuPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "is_modified", nullable = false)
    private Boolean isModified;

    @OneToMany(mappedBy = "cartMenuItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartMenuItemOption> cartMenuItemOptions = new ArrayList<>();

    @Builder
    private CartMenuItem(
        Cart cart,
        OrderableShopMenu orderableShopMenu,
        OrderableShopMenuPrice orderableShopMenuPrice,
        Integer quantity,
        Boolean isModified
    ) {
        this.cart = cart;
        this.orderableShopMenu = orderableShopMenu;
        this.orderableShopMenuPrice = orderableShopMenuPrice;
        this.quantity = quantity;
        this.isModified = isModified;
    }

    public static CartMenuItem create(Cart cart, OrderableShopMenu menu, OrderableShopMenuPrice price, List<OrderableShopMenuOption> options) {
        CartMenuItem cartMenuItem = CartMenuItem.builder()
            .cart(cart)
            .orderableShopMenu(menu)
            .orderableShopMenuPrice(price)
            .quantity(1)
            .isModified(false)
            .build();

        if (options != null) {
            options.forEach(option ->
                cartMenuItem.cartMenuItemOptions.add(CartMenuItemOption.create(cartMenuItem, option))
            );
        }
        return cartMenuItem;
    }

    public void updateQuantity(Integer quantity) {
        if (quantity == null) {
            throw new CartException(CartErrorCode.INVALID_QUANTITY, "수량은 null일 수 없습니다.");
        }
        if (quantity <= 0) {
            throw new CartException(CartErrorCode.INVALID_QUANTITY, "수량은 1 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }

    public void increaseQuantity() {
        this.quantity++;
    }

    public void increaseQuantity(Integer amount) {
        this.quantity += amount;
    }

    public boolean isSameItem(OrderableShopMenu menu, OrderableShopMenuPrice price, List<OrderableShopMenuOption> options) {
        // 메뉴와 가격 ID가 다른 경우
        if (!this.orderableShopMenu.getId().equals(menu.getId()) || !this.orderableShopMenuPrice.getId().equals(price.getId())) {
            return false;
        }

        // 선택한 옵션의 개수가 다른 경우
        if (this.cartMenuItemOptions.size() != options.size()) {
            return false;
        }

        // 선택한 옵션의 구성이 다른 경우 (정렬 후 비교)
        List<Integer> existingOptionIds = this.cartMenuItemOptions.stream()
            .map(opt -> opt.getOrderableShopMenuOption().getId())
            .sorted()
            .toList();

        List<Integer> newOptionIds = options.stream()
            .map(OrderableShopMenuOption::getId)
            .sorted()
            .toList();

        return existingOptionIds.equals(newOptionIds);
    }

    public void updatePriceAndOptions(OrderableShopMenuPrice newPrice, List<OrderableShopMenuOption> newOptions) {
        this.orderableShopMenuPrice = newPrice;
        this.cartMenuItemOptions.clear();
        if (newOptions != null) {
            newOptions.forEach(option -> this.cartMenuItemOptions.add(CartMenuItemOption.create(this, option)));
        }
        this.isModified = true;
    }
}
