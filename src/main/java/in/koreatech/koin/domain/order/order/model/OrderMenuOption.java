package in.koreatech.koin.domain.order.order.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.lang.Boolean.FALSE;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOptionGroup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "koin", name = "order_menu_option_v2")
@NoArgsConstructor(access = PROTECTED)
public class OrderMenuOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @NotBlank
    @Column(name = "group_name", nullable = false, updatable = false)
    private String optionGroupName;

    @NotBlank
    @Column(name = "name", nullable = false, updatable = false)
    private String optionName;

    @NotNull
    @Column(name = "price", nullable = false, updatable = false)
    private Integer optionPrice;

    @NotNull
    @Column(name = "quantity", nullable = false, updatable = false)
    private Integer quantity;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = FALSE;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_menu_id", nullable = false)
    private OrderMenu orderMenu;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "orderable_shop_menu_option_group_id", nullable = false)
    private OrderableShopMenuOptionGroup orderableShopMenuOptionGroup;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "orderable_shop_menu_option_id", nullable = false)
    private OrderableShopMenuOption orderableShopMenuOption;

    @Builder
    private OrderMenuOption(
        String optionGroupName,
        String optionName,
        Integer optionPrice,
        Integer quantity,
        Boolean isDeleted,
        OrderMenu orderMenu,
        OrderableShopMenuOptionGroup orderableShopMenuOptionGroup,
        OrderableShopMenuOption orderableShopMenuOption
    ) {
        this.optionGroupName = optionGroupName;
        this.optionName = optionName;
        this.optionPrice = optionPrice;
        this.quantity = quantity;
        this.isDeleted = isDeleted;
        this.orderMenu = orderMenu;
        this.orderableShopMenuOptionGroup = orderableShopMenuOptionGroup;
        this.orderableShopMenuOption = orderableShopMenuOption;
    }
}
