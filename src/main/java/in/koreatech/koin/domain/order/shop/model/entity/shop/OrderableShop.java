package in.koreatech.koin.domain.order.shop.model.entity.shop;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Where;

import in.koreatech.koin._common.model.BaseEntity;
import in.koreatech.koin.domain.order.cart.exception.CartErrorCode;
import in.koreatech.koin.domain.order.cart.exception.CartException;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuGroup;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "orderable_shop")
@Where(clause = "is_deleted=0")
public class OrderableShop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false, unique = true)
    private Shop shop;

    @Column(name = "delivery", nullable = false)
    private boolean delivery = true;

    @Column(name = "takeout", nullable = false)
    private boolean takeout = true;

    @Column(name = "service_event", nullable = false)
    private boolean serviceEvent = false;

    @Column(name = "minimum_order_amount", nullable = false)
    private Integer minimumOrderAmount;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @BatchSize(size = 7) // 기본 4개 (메인, 추천, 세트, 사이드) + 사장님 커스텀 3개
    @OneToMany(mappedBy = "orderableShop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderableShopMenuGroup> menuGroups = new ArrayList<>();

    @Builder
    public OrderableShop(Integer id, Shop shop, boolean delivery, boolean takeout, boolean serviceEvent,
        Integer minimumOrderAmount, boolean isDeleted, List<OrderableShopMenuGroup> menuGroups) {
        this.id = id;
        this.shop = shop;
        this.delivery = delivery;
        this.takeout = takeout;
        this.serviceEvent = serviceEvent;
        this.minimumOrderAmount = minimumOrderAmount;
        this.isDeleted = isDeleted;
        this.menuGroups = menuGroups;
    }

    public void requireShopOpen() {
        if (shop == null || shop.getShopOperation() == null || !shop.getShopOperation().isOpen()) {
            throw new CartException(CartErrorCode.SHOP_CLOSED);
        }
    }

    public Integer calculateDeliveryFee(Integer orderAmount) {
        return this.shop.getBaseDeliveryTips().calculateDeliveryTip(orderAmount);
    }
}
