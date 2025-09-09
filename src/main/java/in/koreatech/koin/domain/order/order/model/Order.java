package in.koreatech.koin.domain.order.order.model;

import static in.koreatech.koin.domain.order.order.model.OrderStatus.*;
import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.lang.Boolean.FALSE;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Where;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(schema = "koin", name = "order_v2")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    Integer id;

    @NotBlank
    @Size(min = 6, max = 64)
    @Column(name = "pg_order_id", length = 64, nullable = false, updatable = false)
    private String pgOrderId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "orderable_shop_name", length = 255, nullable = false, updatable = false)
    private String orderableShopName;

    @NotBlank
    @Column(name = "orderable_shop_address", nullable = false, updatable = false, columnDefinition = "TEXT")
    private String orderableShopAddress;

    @Column(name = "orderable_shop_address_detail", updatable = false, columnDefinition = "TEXT")
    private String orderableShopAddressDetail;

    @NotNull
    @Enumerated(STRING)
    @Column(name = "order_type", length = 10, nullable = false, updatable = false)
    private OrderType orderType;

    @NotBlank
    @Size(max = 20)
    @Column(name = "phone_number", length = 20, nullable = false, updatable = false)
    private String phoneNumber;

    @NotNull
    @Column(name = "total_product_price", nullable = false, updatable = false)
    private Integer totalProductPrice;

    @Column(name = "discount_amount")
    private Integer discountAmount;

    @NotNull
    @Column(name = "total_price", nullable = false, updatable = false)
    private Integer totalPrice;

    @NotNull
    @Enumerated(STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "canceled_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime canceledAt;

    @Size(max = 200)
    @Column(name = "canceled_reason")
    private String canceledReason;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = FALSE;

    @JoinColumn(name = "orderable_shop_id", nullable = false, updatable = false)
    @ManyToOne(fetch = LAZY)
    private OrderableShop orderableShop;

    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @ManyToOne(fetch = LAZY)
    private User user;

    @OneToOne(mappedBy = "order", fetch = LAZY, cascade = ALL)
    private OrderDelivery orderDelivery;

    @OneToOne(mappedBy = "order", fetch = LAZY, cascade = ALL)
    private OrderTakeout orderTakeout;

    @OneToMany(mappedBy = "order", fetch = LAZY, cascade = ALL, orphanRemoval = true)
    private List<OrderMenu> orderMenus = new ArrayList<>();

    @Builder
    public Order(
        String pgOrderId,
        String orderableShopName,
        String orderableShopAddress,
        String orderableShopAddressDetail,
        OrderType orderType,
        String phoneNumber,
        Integer totalProductPrice,
        Integer discountAmount,
        Integer totalPrice,
        OrderStatus status,
        LocalDateTime canceledAt,
        String canceledReason,
        Boolean isDeleted,
        OrderableShop orderableShop,
        User user,
        OrderDelivery orderDelivery,
        OrderTakeout orderTakeout,
        List<OrderMenu> orderMenus
    ) {
        this.pgOrderId = pgOrderId;
        this.orderableShopName = orderableShopName;
        this.orderableShopAddress = orderableShopAddress;
        this.orderableShopAddressDetail = orderableShopAddressDetail;
        this.orderType = orderType;
        this.phoneNumber = phoneNumber;
        this.totalProductPrice = totalProductPrice;
        this.discountAmount = discountAmount;
        this.totalPrice = totalPrice;
        this.status = status;
        this.canceledAt = canceledAt;
        this.canceledReason = canceledReason;
        this.isDeleted = isDeleted;
        this.orderableShop = orderableShop;
        this.user = user;
        this.orderDelivery = orderDelivery;
        this.orderTakeout = orderTakeout;
        this.orderMenus = orderMenus;
    }

    public void setOrderDelivery(OrderDelivery orderDelivery) {
        this.orderDelivery = orderDelivery;
    }

    public void setOrderTakeout(OrderTakeout orderTakeout) {
        this.orderTakeout = orderTakeout;
    }

    public void addOrderMenu(OrderMenu orderMenu) {
        if (orderMenus == null) {
            orderMenus = new ArrayList<>();
        }
        this.orderMenus.add(orderMenu);
    }

    public void cancel(String cancelReason) {
        this.status = CANCELED;
        this.canceledReason = cancelReason;
        this.canceledAt = LocalDateTime.now();
    }

    public void deliveryComplete() {
        this.status = DELIVERED;
    }

    public void takeoutComplete() {
        this.status = PACKAGED;
    }
}
