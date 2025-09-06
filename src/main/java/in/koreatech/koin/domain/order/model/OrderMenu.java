package in.koreatech.koin.domain.order.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.lang.Boolean.FALSE;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "koin", name = "order_menu_v2")
@NoArgsConstructor(access = PROTECTED)
public class OrderMenu extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotBlank
    @Column(name = "name", nullable = false, updatable = false)
    private String menuName;

    @NotNull
    @Column(name = "price", nullable = false, updatable = false)
    private Integer menuPrice;

    @Column(name = "price_name", updatable = false)
    private String menuPriceName;

    @NotNull
    @Column(name = "quantity", nullable = false, updatable = false)
    private Integer quantity;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = FALSE;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "orderable_shop_menu_id", nullable = false, updatable = false)
    private OrderableShopMenu orderableShopMenu;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "orderable_shop_menu_price_id", nullable = false, updatable = false)
    private OrderableShopMenuPrice orderableShopMenuPrice;

    @OneToMany(mappedBy = "orderMenu", cascade = ALL, orphanRemoval = true)
    private List<OrderMenuOption> orderMenuOptions = new ArrayList<>();

    @Builder
    private OrderMenu(
        String menuName,
        Integer menuPrice,
        String menuPriceName,
        Integer quantity,
        Boolean isDeleted,
        Order order,
        OrderableShopMenu orderableShopMenu,
        OrderableShopMenuPrice orderableShopMenuPrice,
        List<OrderMenuOption> orderMenuOptions
    ) {
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.menuPriceName = menuPriceName;
        this.quantity = quantity;
        this.isDeleted = isDeleted;
        this.order = order;
        this.orderableShopMenu = orderableShopMenu;
        this.orderableShopMenuPrice = orderableShopMenuPrice;
        this.orderMenuOptions = orderMenuOptions;
    }

    public void setOrderMenuOptions(List<OrderMenuOption> orderMenuOptions) {
        this.orderMenuOptions = orderMenuOptions;
    }
}
